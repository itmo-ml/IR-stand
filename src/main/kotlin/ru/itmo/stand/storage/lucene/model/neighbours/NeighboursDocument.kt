package ru.itmo.stand.storage.lucene.model.neighbours

data class NeighboursDocument(
    val token: String,
    val tokenWithEmbeddingId: String,
    val docId: String,
    val score: Float,
)
