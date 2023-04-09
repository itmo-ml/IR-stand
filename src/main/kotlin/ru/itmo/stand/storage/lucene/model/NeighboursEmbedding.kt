package ru.itmo.stand.storage.lucene.model

data class NeighboursEmbedding(
    val docId: String,
    val embedding: FloatArray,
)
