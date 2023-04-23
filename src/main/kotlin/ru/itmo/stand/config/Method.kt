package ru.itmo.stand.config

import ru.itmo.stand.storage.lucene.model.DocumentBm25
import ru.itmo.stand.storage.lucene.model.DocumentSnrm

enum class Method(val indexName: String) {
    BM25(DocumentBm25.DOCUMENT_BM25),
    SNRM(DocumentSnrm.DOCUMENT_SNRM),
    CUSTOM("TODO"), // TODO: add index name
    BERT_NSP("bert_nsp_prediction"),
    BERT_MULTI_TOKEN("bert_multi_token"),
    NEIGHBOURS("TODO"),
}
