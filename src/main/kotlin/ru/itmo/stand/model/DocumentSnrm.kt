package ru.itmo.stand.model

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import ru.itmo.stand.model.DocumentSnrm.Companion.DOCUMENT_SNRM

@Document(indexName = DOCUMENT_SNRM)
//@Setting(settingPath = "document_snrm_index_settings.json")
data class DocumentSnrm(
    @Id
    var id: String? = null,

    val content: String,
) {
    companion object {
        const val DOCUMENT_SNRM = "document_snrm"
    }
}
