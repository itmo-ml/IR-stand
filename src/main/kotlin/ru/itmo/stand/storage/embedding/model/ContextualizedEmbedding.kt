package ru.itmo.stand.storage.embedding.model

import com.github.jelmerk.knn.Item

data class ContextualizedEmbedding(
    val tokenWithEmbeddingId: String,
    val embedding: FloatArray,
) : Item<String, FloatArray> {

    override fun id(): String = tokenWithEmbeddingId
    override fun vector(): FloatArray = embedding
    override fun dimensions(): Int = embedding.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContextualizedEmbedding

        if (tokenWithEmbeddingId != other.tokenWithEmbeddingId) return false
        return embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = tokenWithEmbeddingId.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ContextualizedEmbedding(tokenWithEmbeddingId='$tokenWithEmbeddingId')"
    }

    companion object {
        const val TOKEN_AND_EMBEDDING_ID_SEPARATOR = ":"
    }
}
