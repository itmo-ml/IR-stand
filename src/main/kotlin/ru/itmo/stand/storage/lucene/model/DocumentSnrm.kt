package ru.itmo.stand.storage.lucene.model

data class DocumentSnrm(
    var id: String? = null,
    val externalId: Long?,
    val weights: FloatArray,
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
