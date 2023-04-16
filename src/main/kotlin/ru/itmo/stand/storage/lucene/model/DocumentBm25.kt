package ru.itmo.stand.storage.lucene.model

data class DocumentBm25(
    val id: String,
    val content: String,
) {
    companion object {
        const val DOCUMENT_BM25 = "document_bm25"
    }
}
