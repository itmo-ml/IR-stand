package ru.itmo.stand.service.impl.custom

import io.github.oshai.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.DefaultBertTranslator
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.util.dot
import ru.itmo.stand.util.extractId

@Service
@ConditionalOnProperty(value = ["stand.app.method"], havingValue = "custom")
class DocumentCustomService(bertTranslator: DefaultBertTranslator) : BaseBertService(bertTranslator) {

    private val log = KotlinLogging.logger { }

    /**
     * CLI command example: save -m CUSTOM "Around 9 Million people live in London"
     */
    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (documentId, passage) = extractId(content)
        val tokens = preprocess(passage)
        val passageVector = predictor.predict(passage)
        // It is assumed that only uni-grams and bi-grams are used.
        // Since [Batchifier.STACK] is used, the input forms must be the same.
        // TODO: investigate the use of [PaddingStackBatchifier].
        val vectorByTokenPairs = tokens.partition { it.split(" ").size == 1 }
            .let { predictor.batchPredict(it.first).zip(it.first) + predictor.batchPredict(it.second).zip(it.second) }

        vectorByTokenPairs.forEach { (tokenVector, token) ->
            invertedIndex.index(token, tokenVector dot passageVector, documentId)
        }

        log.info { "Content is indexed (id=$documentId)" }
        return documentId
    }
}
