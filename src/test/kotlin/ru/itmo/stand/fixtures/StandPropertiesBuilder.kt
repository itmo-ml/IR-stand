package ru.itmo.stand.fixtures

import ru.itmo.stand.config.BertModelType
import ru.itmo.stand.config.EmbeddingStorageType
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.config.StandProperties.AnnAlgorithm
import ru.itmo.stand.config.StandProperties.ApplicationProperties
import ru.itmo.stand.config.StandProperties.BertMultiToken
import ru.itmo.stand.config.StandProperties.NeighboursAlgorithm

fun standProperties(
    basePath: String = ".",
    bertMultiTokenBatchSize: Int = 5,
    neighboursAlgorithmBatchSize: Int = 5,
) = StandProperties(
    ApplicationProperties(
        Method.NEIGHBOURS,
        basePath,
        BertMultiToken(bertMultiTokenBatchSize),
        NeighboursAlgorithm(
            windowSize = neighboursAlgorithmBatchSize,
            bertModelType = BertModelType.BASE,
            bertWindowBatchSize = 1000,
            documentsCount = 500_000,
            embeddingStorage = EmbeddingStorageType.IN_MEMORY,
        ),
        AnnAlgorithm(
            bertModelType = BertModelType.BASE,
        ),
    ),
)
