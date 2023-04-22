package ru.itmo.stand.storage.lucene.model.neighbours

data class NeighboursEmbedding(
    val docId: String,
    val embedding: FloatArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NeighboursEmbedding

        if (docId != other.docId) return false
        return embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = docId.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
