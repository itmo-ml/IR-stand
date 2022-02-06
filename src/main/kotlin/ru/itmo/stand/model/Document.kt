package ru.itmo.stand.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Setting

@Document(indexName = "document")
@Setting(settingPath = "document_index_settings.json")
data class Document(
    @Id
    var id: String? = null,

    val content: String,
)
