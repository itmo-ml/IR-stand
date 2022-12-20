package ru.itmo.stand.storage.mongodb.model.neighbours

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("ContextualizedVector")
data class ContextualizedVector(
    @Id
    val id: String? = null,
    @Indexed
    val token: String,
    val documentId: String,
    val vector: FloatArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContextualizedVector

        if (id != other.id) return false
        if (token != other.token) return false
        if (documentId != other.documentId) return false
        if (!vector.contentEquals(other.vector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + token.hashCode()
        result = 31 * result + documentId.hashCode()
        result = 31 * result + vector.contentHashCode()
        return result
    }
}
