package ru.itmo.stand.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.storage.lucene.model.DocumentBm25
import ru.itmo.stand.storage.lucene.repository.DocumentBm25Repository
import ru.itmo.stand.util.createPath
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.formatMrr
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.throwDocIdNotFoundEx
import java.io.File

@Service
class DocumentBm25Service(private val documentBm25Repository: DocumentBm25Repository) : DocumentService {

    @Autowired
    protected lateinit var standProperties: StandProperties

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override val method: Method
        get() = Method.BM25

    override fun find(id: String): String? = TODO()

    override fun search(queries: File, format: Format): List<String> = when (format) {
        Format.JUST_QUERY -> {
            documentBm25Repository.findByContent(queries.readLines().single(), 10)
                .map { it.id ?: throwDocIdNotFoundEx() }
        }

        Format.MS_MARCO -> {
            searchWithMS_Marco(queries)
        }
    }

    private fun searchWithMS_Marco(queries: File): List<String> {
        val queryByIdMap = getQueryByIdMap(queries)
        val outputLines = mutableListOf<String>()

        for ((queryId, query) in queryByIdMap) {
            val docsTopList = documentBm25Repository.findByContent(query, 10)
                .mapIndexed { index, docId -> formatMrr(queryId, docId.id.toString(), index + 1) }

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
        val processedModel = DocumentBm25(id = externalId, content = passage)
        documentBm25Repository.save(processedModel)
        return "Document saved"
    }

    override fun saveInBatch(contents: File, withId: Boolean): List<String> {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        log.info("Total size: ${contents.size}")
        val chunkSize = 10_000
        for ((index, chunk) in contents.lineSequence().chunked(chunkSize).withIndex()) {
            val processedModels = chunk.map {
                val (externalId, passage) = extractId(it)
                DocumentBm25(id = externalId, content = passage)
            }
            documentBm25Repository.saveAll(processedModels)
            log.info("Processed: ${(index + 1) * chunkSize}")
        }
        documentBm25Repository.completeSaving()
        return emptyList()
    }

    override fun getFootprint(): String = TODO()
}
