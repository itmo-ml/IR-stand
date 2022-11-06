package ru.itmo.stand.service.impl.bertmultitoken

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.service.impl.bertnsp.BertNspTranslator
import ru.itmo.stand.util.TOKEN_SEPARATOR
import ru.itmo.stand.util.createContexts
import ru.itmo.stand.util.toTokens
import java.util.concurrent.ConcurrentHashMap

@Service
class DocumentBertMultiTokenService(
        private val bertNspTranslator: BertNspTranslator,
        private val stanfordCoreNlp: StanfordCoreNLP,
): BaseBertService(bertNspTranslator) {


    override val method: Method
        get() = Method.BERT_MULTI_TOKEN

    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (documentId, passage) = extractId(content)

        val tokens = preprocess(passage);

        val scores = tokens.createContexts(standProperties.app.bertMultiToken.tokenBatchSize).flatMap { window ->
            val modelInput =  concatNsp(window.joinToString(" "), passage)
            val score = predictor.predict(concatNsp(modelInput, passage))[0]
            window.map { Pair(it, score) }
        }
                .groupBy   { it.first }
                .mapValues { it.value.map { pair -> pair.second }.average().toFloat() }

        scores.forEach{ (token, score) -> invertedIndex.index(token, score, documentId)}

        log.info("Content is indexed (id={})", documentId)
        return documentId
    }

    private fun concatNsp(token: String, passage: String): String {
        return "$token $TOKEN_SEPARATOR $passage"
    }

    override fun preprocess(contents: List<String>): List<List<String>> = contents
            .map { it.lowercase() }
            .map {it.toTokens(stanfordCoreNlp)}

}

