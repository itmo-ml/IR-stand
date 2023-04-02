package ru.itmo.stand.storage.lucene.model

data class DocumentBm25(
    var id: String? = null,
    val content: String,
) {
    companion object {
        const val DOCUMENT_BM25 = "document_bm25"
    }
}
