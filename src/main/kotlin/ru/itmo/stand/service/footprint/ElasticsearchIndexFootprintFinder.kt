package ru.itmo.stand.service.footprint

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.util.formatBytesToReadable

@Service
class ElasticsearchIndexFootprintFinder(
    private val standProperties: StandProperties,
    private val objectMapper: ObjectMapper,
) {

    fun findFootprint(indexName: String): String {
        val totalIndexStats = RestTemplate().getForObject(
            "http://${standProperties.elasticsearch.hostAndPort}/$indexName/_stats",
            String::class.java,
        ).let { objectMapper.readTree(it) }
            .get("indices")
            ?.get(indexName)
            ?.get("total") ?: throw IllegalStateException("Total $indexName index stats not found.")

        val storeUsage = totalIndexStats.get("store").get("size_in_bytes").asLong().formatBytesToReadable()
        val memoryUsage = (
            totalIndexStats.get("fielddata").get("memory_size_in_bytes").asLong() +
                totalIndexStats.get("completion").get("size_in_bytes").asLong() +
                totalIndexStats.get("segments").get("memory_in_bytes").asLong() +
                totalIndexStats.get("query_cache").get("memory_size_in_bytes").asLong() +
                totalIndexStats.get("request_cache").get("memory_size_in_bytes").asLong()
            ).formatBytesToReadable()

        return """
            HDD: $storeUsage
            RAM: $memoryUsage
        """.trimIndent()
    }
}
