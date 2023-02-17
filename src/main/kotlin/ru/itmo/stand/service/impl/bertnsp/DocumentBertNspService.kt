package ru.itmo.stand.service.impl.bertnsp

import edu.stanford.nlp.naturalli.ClauseSplitter.log
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.bert.BertNspTranslator
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.util.TOKEN_SEPARATOR
import ru.itmo.stand.util.extractId

@Service
@Profile("!standalone")
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

        val tokens = preprocess(passage)

        val scoreByTokens = tokens
            .map { Pair(it, predictor.predict(concatNsp(it, passage))[0]) }

        scoreByTokens.forEach { (token, score) -> invertedIndex.index(token, score, documentId) }

        log.info("Content is indexed (id={})", documentId)
        return documentId
    }

    override fun saveStream(contents: Sequence<String>, withId: Boolean): List<String> {
        TODO("Not yet implemented")
    }

    private fun concatNsp(token: String, passage: String): String {
        return "$token $TOKEN_SEPARATOR $passage"
    }
}
