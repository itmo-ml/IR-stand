package ru.itmo.stand.index.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Setting
import ru.itmo.stand.index.model.DocumentBm25.Companion.DOCUMENT_BM25

@Document(indexName = DOCUMENT_BM25)
@Setting(settingPath = "document_bm25_index_settings.json")
data class DocumentBm25(
    @Id
    var id: String? = null,

    val content: String,
) {
    companion object {
        const val DOCUMENT_BM25 = "document_bm25"
    }
}
