package ru.itmo.stand.service.impl.bertnsp

import edu.stanford.nlp.naturalli.ClauseSplitter.log
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.util.TOKEN_SEPARATOR

@Service
class DocumentBertNspService(
    bertNspTranslator: BertNspTranslator,
) : BaseBertService(bertNspTranslator) {

    override val method: Method
        get() = Method.BERT_NSP

    /**
     * CLI command example: save -m BERT_NSP "Around 9 Million people live in London"
     */
    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (documentId, passage) = extractId(content)

        val tokens = preprocess(passage);

        val scoreByTokens = tokens
            .map { Pair(it, predictor.predict(concatNsp(it, passage))[0]) }

        scoreByTokens.forEach { (token, score) -> invertedIndex.index(token, score, documentId) }

        log.info("Content is indexed (id={})", documentId)
        return documentId
    }

    private fun concatNsp(token: String, passage: String): String {
        return "$token $TOKEN_SEPARATOR $passage"
    }
}
