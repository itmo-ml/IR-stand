package ru.itmo.stand.service.sqlite


data class SqliteDocument(
    val groupKey: String,
    val documentId: String,
    val content: String,
)
