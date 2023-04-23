package ru.itmo.stand.storage.embedding.model

data class ContextualizedEmbedding(
    val token: String, // TODO: combine token and embId
    val embeddingId: Int,
    val embedding: Array<Float>,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContextualizedEmbedding

        if (token != other.token) return false
        if (embeddingId != other.embeddingId) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
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
}
