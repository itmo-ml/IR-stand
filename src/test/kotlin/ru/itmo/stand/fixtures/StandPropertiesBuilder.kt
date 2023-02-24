package ru.itmo.stand.fixtures

import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.config.StandProperties.ApplicationProperties
import ru.itmo.stand.config.StandProperties.BertMultiToken
import ru.itmo.stand.config.StandProperties.ElasticsearchProperties
import ru.itmo.stand.config.StandProperties.NeighboursAlgorithm

fun standProperties(
    elkHostAndPort: String = "localhost:9200",
    basePath: String = ".",
    bertMultiTokenBatchSize: Int = 5,
    neighboursAlgorithmBatchSize: Int = 5,
) = StandProperties(
    ElasticsearchProperties(elkHostAndPort),
    ApplicationProperties(
        basePath,
        BertMultiToken(bertMultiTokenBatchSize),
        NeighboursAlgorithm(neighboursAlgorithmBatchSize),
    ),
)
