package ru.itmo.stand.fixtures

import ru.itmo.stand.config.BertModelType
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.config.StandProperties.ApplicationProperties
import ru.itmo.stand.config.StandProperties.BertMultiToken
import ru.itmo.stand.config.StandProperties.NeighboursAlgorithm

fun standProperties(
    basePath: String = ".",
    bertMultiTokenBatchSize: Int = 5,
    neighboursAlgorithmBatchSize: Int = 5,
) = StandProperties(
    ApplicationProperties(
        basePath,
        BertMultiToken(bertMultiTokenBatchSize),
        NeighboursAlgorithm(neighboursAlgorithmBatchSize, BertModelType.BASE, 500_000),
    ),
)
