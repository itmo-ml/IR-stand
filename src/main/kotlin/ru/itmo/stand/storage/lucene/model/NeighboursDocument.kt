package ru.itmo.stand.storage.lucene.model

data class NeighboursDocument(
    val tokenWithEmbeddingId: String,
    val docId: String,
    val score: Float,
)
