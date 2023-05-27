package ru.itmo.stand.storage.embedding.model.ann

import com.github.jelmerk.knn.Item

data class DocumentEmbedding(
    val id: String,
    val embedding: FloatArray,
) : Item<String, FloatArray> {

    override fun id(): String = id
    override fun vector(): FloatArray = embedding
    override fun dimensions(): Int = embedding.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentEmbedding

        if (id != other.id) return false
        return embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
