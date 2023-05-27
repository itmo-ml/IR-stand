package ru.itmo.stand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(value = "stand")
data class StandProperties @ConstructorBinding constructor(
    val app: ApplicationProperties,
) {

    data class ApplicationProperties(
        val method: Method,
        val basePath: String,
        val bertMultiToken: BertMultiToken,
        val neighboursAlgorithm: NeighboursAlgorithm,
        val annAlgorithm: AnnAlgorithm,
    )

    data class BertMultiToken(
        val tokenBatchSize: Int,
    )

    data class NeighboursAlgorithm(
        val windowSize: Int,
        val bertModelType: BertModelType,
        val bertWindowBatchSize: Int,
        val documentsCount: Int,
        val embeddingStorage: EmbeddingStorageType,
    )

    data class AnnAlgorithm(
        val bertModelType: BertModelType,
    )
}
