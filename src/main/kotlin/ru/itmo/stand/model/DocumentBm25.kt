package ru.itmo.stand.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Setting

@Document(indexName = "document_bm25")
@Setting(settingPath = "document_bm25_index_settings.json")
data class DocumentBm25(
    @Id
    var id: String? = null,

    val content: String,
)
