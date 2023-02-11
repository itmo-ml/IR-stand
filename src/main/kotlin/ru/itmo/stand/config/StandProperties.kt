package ru.itmo.stand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = "stand")
data class StandProperties(
    val elasticsearch: ElasticsearchProperties,
    val app: ApplicationProperties
) {
    data class ElasticsearchProperties(
        val hostAndPort: String,
    )

    data class ApplicationProperties(
        val basePath: String,
        val bertMultiToken: BertMultiToken,
        val neighboursAlgorithm: NeighboursAlgorithm,
    )

    data class BertMultiToken(
        val tokenBatchSize: Int,
    )

    data class NeighboursAlgorithm(
        val tokenBatchSize: Int,
    )


}
