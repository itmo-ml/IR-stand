package ru.itmo.stand.storage.embedding.model

import com.github.jelmerk.knn.Item

data class ContextualizedEmbedding(
    val token: String, // TODO: combine token and embId
    val embeddingId: Int,
    val embedding: FloatArray,
): Item<Int, FloatArray> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContextualizedEmbedding

        if (token != other.token) return false
        if (embeddingId != other.embeddingId) return false
        return embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + embeddingId
        result = 31 * result + embedding.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ContextualizedEmbedding(token='$token', embeddingId=$embeddingId)"
    }

    override fun id(): Int {
        return embeddingId
    }

    override fun vector(): FloatArray {
        return embedding
    }

    override fun dimensions(): Int {
        return embedding.size
    }
}
