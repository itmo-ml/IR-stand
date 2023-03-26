package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.footprint.ElasticsearchIndexFootprintFinder
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.storage.elasticsearch.model.DocumentBm25
import ru.itmo.stand.storage.elasticsearch.repository.DocumentBm25Repository
import ru.itmo.stand.util.createPath
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.formatMrr
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.throwDocIdNotFoundEx
import java.io.File

@Profile("!standalone")
@Service
class DocumentBm25Service(
    private val elasticsearchIndexFootprintFinder: ElasticsearchIndexFootprintFinder,
    private val documentBm25Repository: DocumentBm25Repository,
    private val stanfordCoreNlp: StanfordCoreNLP,
) : DocumentService {

    @Autowired
    protected lateinit var standProperties: StandProperties

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override val method: Method
        get() = Method.BM25

    override fun find(id: String): String? = documentBm25Repository.findByIdOrNull(id)?.content

    override fun search(queries: File, format: Format): List<String> = when (format) {
        Format.JUST_QUERY -> { val processedQuery = preprocess(queries.readLines().single())
            /*return*/ documentBm25Repository.findByRepresentation(processedQuery, Pageable.unpaged())
                .map { it.id ?: throwDocIdNotFoundEx() } }
        Format.MS_MARCO -> { searchWithMS_Marco(queries) }
    }

    private fun searchWithMS_Marco(queries: File): List<String> {
        val queryByIdMap = getQueryByIdMap(queries)
        val outputLines = mutableListOf<String>()

        for ((queryId, query) in queryByIdMap) {
            // documentBm25Repository
            var docsTopList = documentBm25Repository.findByRepresentation(query, PageRequest.of(0, 10))
                .mapIndexed { index, docId -> formatMrr(queryId, docId.externalId.toString(), index + 1) }

            outputLines.addAll(docsTopList)
        }
        val outputPath = "${standProperties.app.basePath}/outputs/${method.name.lowercase()}/queriesForMRR.tsv"
        File(outputPath).createPath().bufferedWriter()
            .use { output -> outputLines.forEach { line -> output.appendLine(line) } }
        return listOf("See output in $outputPath")
    }

    private fun getQueryByIdMap(queries: File): Map<Int, String> = queries.bufferedReader()
        .use { it.readLines() }
        .filter { it != "" }
        .map { it.split("\t") }
        .associate { it[0].toInt() to it[1] }

    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (externalId, passage) = extractId(content)
        val processedModel = DocumentBm25(content = passage, representation = preprocess(passage), externalId = externalId.toLong())
        return documentBm25Repository.save(processedModel).id ?: throwDocIdNotFoundEx()
    }

    override fun saveInBatch(contents: File, withId: Boolean): List<String> {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        for (chunk in contents.lineSequence().chunked(1000)) {
            val processedModels = chunk.map {
                val (externalId, passage) = extractId(it)
                DocumentBm25(content = it, representation = preprocess(passage), externalId = externalId.toLong())
            }
            documentBm25Repository.saveAll(processedModels)
            log.info("Index now holds ${documentBm25Repository.count()} documents")
        }

        return emptyList()
    }

    override fun getFootprint(): String = elasticsearchIndexFootprintFinder.findFootprint(method.indexName)

    private fun preprocess(content: String) =
        stanfordCoreNlp.processToCoreDocument(content)
            .tokens()
            .joinToString(" ") { it.lemma() }
}
