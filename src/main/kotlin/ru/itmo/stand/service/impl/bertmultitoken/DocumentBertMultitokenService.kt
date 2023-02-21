package ru.itmo.stand.service.impl.bertmultitoken

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.bert.BertNspTranslator
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.util.TOKEN_SEPARATOR
import ru.itmo.stand.util.createWindows
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.toTokens

@Service
@Profile("!standalone")
class DocumentBertMultiTokenService(
    private val stanfordCoreNlp: StanfordCoreNLP,
    bertNspTranslator: BertNspTranslator,
) : BaseBertService(bertNspTranslator) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override val method: Method
        get() = Method.BERT_MULTI_TOKEN

    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (documentId, passage) = extractId(content)

        val tokens = preprocess(passage);

        val scores = tokens.createWindows(standProperties.app.bertMultiToken.tokenBatchSize).flatMap { window ->
            val modelInput = concatNsp(window.content.joinToString(" "), passage)
            val score = predictor.predict(concatNsp(modelInput, passage))[0]
            window.content.map { Pair(it, score) }
        }
            .groupBy { it.first }
            .mapValues { it.value.map { pair -> pair.second }.average().toFloat() }

        scores.forEach { (token, score) -> invertedIndex.index(token, score, documentId) }

        log.info("Content is indexed (id={})", documentId)
        return documentId
    }

    private fun concatNsp(token: String, passage: String): String {
        return "$token $TOKEN_SEPARATOR $passage"
    }

    override fun preprocess(contents: List<String>): List<List<String>> = contents
        .map { it.lowercase() }
        .map { it.toTokens(stanfordCoreNlp) }

}

