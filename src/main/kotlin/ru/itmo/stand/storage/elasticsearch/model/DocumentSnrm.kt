package ru.itmo.stand.storage.elasticsearch.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import ru.itmo.stand.storage.elasticsearch.model.DocumentSnrm.Companion.DOCUMENT_SNRM

@Document(indexName = DOCUMENT_SNRM)
data class DocumentSnrm(
    @Id
    var id: String? = null,

    @Field(index = false, type = FieldType.Long)
    val externalId: Long?,

    @Field(index = false, type = FieldType.Text)
    val weights: FloatArray,

    @Field
    val representation: String,
) {
    companion object {
        const val DOCUMENT_SNRM = "document_snrm"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentSnrm

        if (id != other.id) return false
        if (externalId != other.externalId) return false
        if (!weights.contentEquals(other.weights)) return false
        if (representation != other.representation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (externalId?.hashCode() ?: 0)
        result = 31 * result + weights.contentHashCode()
        result = 31 * result + representation.hashCode()
        return result
    }
}
