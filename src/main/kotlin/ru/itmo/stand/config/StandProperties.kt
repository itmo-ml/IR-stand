package ru.itmo.stand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(value = "stand")
data class StandProperties @ConstructorBinding constructor(
    val elasticsearch: ElasticsearchProperties,
    val app: ApplicationProperties,
) {
    data class ElasticsearchProperties(
        val hostAndPort: String,
    )

    data class ApplicationProperties(
        val basePath: String,
        val bertMultiToken: BertMultiToken,
        val neighboursAlgorithm: NeighboursAlgorithm,
        val fileLoadBufferSizeKb: Int,
    )

    data class BertMultiToken(
        val tokenBatchSize: Int,
    )

    data class NeighboursAlgorithm(
        val tokenBatchSize: Int,
        val bertModelType: BertModelType,
        val documentsCount: Int,
    )
}
