package ru.itmo.stand.storage.embedding.neighbours

import ru.itmo.stand.storage.embedding.neighbours.model.ContextualizedEmbedding

interface ContextualizedEmbeddingRepository {
    fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding>
    fun index(embedding: ContextualizedEmbedding)
    fun indexBatch(embeddings: List<ContextualizedEmbedding>)
}
