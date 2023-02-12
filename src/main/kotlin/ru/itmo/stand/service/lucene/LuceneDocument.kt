package ru.itmo.stand.service.lucene

data class LuceneDocument(
    val groupKey: String,
    val documentId: String,
    val content: String,
)
