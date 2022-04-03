package ru.itmo.stand.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.commons.io.FileUtils.byteCountToDisplaySize
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.model.DocumentBm25
import ru.itmo.stand.model.DocumentBm25.Companion.DOCUMENT_BM25
import ru.itmo.stand.repository.DocumentBm25Repository
import ru.itmo.stand.service.DocumentService

@Service
class DocumentBm25Service(
    private val documentBm25Repository: DocumentBm25Repository,
    private val standProperties: StandProperties,
    private val stanfordCoreNlp: StanfordCoreNLP,
    private val objectMapper: ObjectMapper,
) : DocumentService {

    override val method: Method
        get() = Method.BM25

    override fun find(id: String): String? = documentBm25Repository.findByIdOrNull(id)?.content

    override fun search(query: String): List<String> {
        val processedQuery = preprocess(query)
        return documentBm25Repository.findByContent(processedQuery)
            .map { it.id ?: throwDocIdNotFoundEx() }
    }

    override fun save(content: String): String {
        val processedModel = DocumentBm25(content = preprocess(content))
        return documentBm25Repository.save(processedModel).id ?: throwDocIdNotFoundEx()
    }

    override fun saveInBatch(contents: List<String>): List<String> {
        val processedModels = contents.map { DocumentBm25(content = preprocess(it)) }
        return documentBm25Repository.saveAll(processedModels).map { it.id ?: throwDocIdNotFoundEx() }
    }

    override fun getFootprint(): String? {
        val requestResult = RestTemplate().getForObject(
            "http://${standProperties.elasticsearch.hostAndPort}/$DOCUMENT_BM25/_stats",
            String::class.java
        )

        val totalIndexStats = objectMapper.readTree(requestResult)
            .get("indices")
            ?.get(DOCUMENT_BM25)
            ?.get("total") ?: throw IllegalStateException("Total $DOCUMENT_BM25 index stats not found.")

        return """
            HDD: ${byteCountToDisplaySize(totalIndexStats.get("store").get("size_in_bytes").asLong())}
            RAM: ${byteCountToDisplaySize(totalIndexStats.get("segments").get("memory_in_bytes").asLong())}
        """.trimIndent()
    }

    private fun preprocess(text: String) =
        stanfordCoreNlp.processToCoreDocument(text)
            .tokens()
            .joinToString(" ") { it.lemma() }
}
