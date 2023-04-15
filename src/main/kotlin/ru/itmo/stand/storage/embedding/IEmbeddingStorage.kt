package ru.itmo.stand.storage.embedding

import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding

interface IEmbeddingStorage {

    fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding>

    fun deleteAllModels(): Boolean

    fun index(embedding: ContextualizedEmbedding)

    fun indexBatch(embeddings: List<ContextualizedEmbedding>)

    fun initialize(): Boolean

    fun loadIndex()
    fun saveIndex()
}
