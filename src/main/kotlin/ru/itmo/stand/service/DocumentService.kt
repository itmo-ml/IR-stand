package ru.itmo.stand.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.RestTemplate
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties

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
        val requestResult = RestTemplate().postForObject(
            "http://${standProperties.elasticsearch.hostAndPort}/$indexName/_disk_usage?run_expensive_tasks=true",
            null,
            String::class.java
        )

        return objectMapper.readTree(requestResult)
            .get(indexName)
            ?.get("store_size")
            ?.asText() ?: throw IllegalStateException("$indexName footprint not found.")
    }

    fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")

    fun extractId(content: String, withId: Boolean): Pair<Long?, String> {
        return if (withId) {
            val idAndPassage = content.split("\t");
            if (idAndPassage.size != 2) {
                throw IllegalStateException("With id option was specified but no id was found")
            }
            Pair(idAndPassage[0].toLong(), idAndPassage[1]);
        } else {
            Pair(null, content);
        }
    }
}
