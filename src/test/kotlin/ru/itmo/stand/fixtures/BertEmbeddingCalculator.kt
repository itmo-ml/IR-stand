package ru.itmo.stand.fixtures

import ru.itmo.stand.service.bert.BertEmbeddingCalculator

fun bertEmbeddingCalculator(): BertEmbeddingCalculator = BertEmbeddingCalculator(
    bertModelLoader(),
    standProperties(),
)
