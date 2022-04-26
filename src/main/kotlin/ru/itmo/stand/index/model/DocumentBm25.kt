package ru.itmo.stand.index.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Setting
import ru.itmo.stand.index.model.DocumentBm25.Companion.DOCUMENT_BM25

@Document(indexName = DOCUMENT_BM25)
@Setting(settingPath = "document_bm25_index_settings.json")
data class DocumentBm25(
    @Id
    var id: String? = null,

    @Field(index = false, type = FieldType.Text)
    val content: String,

    @Field(index = false, type = FieldType.Text)
    val representation: String,

    @Field(index = false, type = FieldType.Long)
    val externalId: Long?
) {
    companion object {
        const val DOCUMENT_BM25 = "document_bm25"
    }
}
