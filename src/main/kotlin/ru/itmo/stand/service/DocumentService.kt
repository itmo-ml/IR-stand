package ru.itmo.stand.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.RestTemplate
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.util.formatBytesToReadable

abstract class DocumentService {

    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var standProperties: StandProperties

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    abstract val method: Method

    abstract fun find(id: String): String?

    abstract fun search(query: String): List<String>

    abstract fun save(content: String, withId: Boolean): String

    abstract fun saveInBatch(contents: List<String>, withId: Boolean): List<String>

    fun getFootprint(): String {
        val indexName = method.indexName
        val totalIndexStats = RestTemplate().getForObject(
            "http://${standProperties.elasticsearch.hostAndPort}/$indexName/_stats",
            String::class.java
        ).let { objectMapper.readTree(it) }
            .get("indices")
            ?.get(indexName)
            ?.get("total") ?: throw IllegalStateException("Total $indexName index stats not found.")

        val storeUsage = totalIndexStats.get("store").get("size_in_bytes").asLong().formatBytesToReadable()
        val memoryUsage = (totalIndexStats.get("fielddata").get("memory_size_in_bytes").asLong() +
            totalIndexStats.get("completion").get("size_in_bytes").asLong() +
            totalIndexStats.get("segments").get("memory_in_bytes").asLong() +
            totalIndexStats.get("query_cache").get("memory_size_in_bytes").asLong() +
            totalIndexStats.get("request_cache").get("memory_size_in_bytes").asLong()).formatBytesToReadable()

        return """
            HDD: $storeUsage
            RAM: $memoryUsage
        """.trimIndent()
    }

    fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")

    fun extractId(content: String, withId: Boolean): Pair<Long?, String> = if (withId)
        extractId(content) { it.toLong() }
    else Pair(null, content)

    fun <T> extractId(content: String, idTransform: (String) -> T): Pair<T, String> {
        val idAndPassage = content.split("\t")
        if (idAndPassage.size != 2) {
            throw IllegalStateException("With id option was specified but no id was found")
        }
        return Pair(idTransform(idAndPassage[0]), idAndPassage[1])
    }
}
