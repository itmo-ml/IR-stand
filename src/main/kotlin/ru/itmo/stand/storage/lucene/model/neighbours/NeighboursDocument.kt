package ru.itmo.stand.storage.lucene.model.neighbours

data class NeighboursDocument(
    val tokenWithEmbeddingId: String,
    val docId: String,
    val score: Float,
)
