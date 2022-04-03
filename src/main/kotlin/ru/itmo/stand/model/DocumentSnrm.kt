package ru.itmo.stand.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import ru.itmo.stand.model.DocumentSnrm.Companion.DOCUMENT_SNRM

@Document(indexName = DOCUMENT_SNRM)
data class DocumentSnrm(
    @Id
    var id: String? = null,

    @Field(index = false)
    val content: String,

    @Field
    val representation: String,
) {
    companion object {
        const val DOCUMENT_SNRM = "document_snrm"
    }
}
