package ru.itmo.stand.service.impl.bertmultitoken

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import io.github.oshai.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertNspTranslator
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.util.TOKEN_SEPARATOR
import ru.itmo.stand.util.createWindows
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.toTokens

@Service
@ConditionalOnProperty(value = ["stand.app.method"], havingValue = "bert_multi_token")
class DocumentBertMultiTokenService(
    private val stanfordCoreNlp: StanfordCoreNLP,
    bertNspTranslator: BertNspTranslator,
) : BaseBertService(bertNspTranslator) {

    private val log = KotlinLogging.logger { }

    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (documentId, passage) = extractId(content)

        val tokens = preprocess(passage)

        val scores = tokens.createWindows(standProperties.app.bertMultiToken.tokenBatchSize).flatMap { window ->
            val modelInput = concatNsp(window.content.joinToString(" "), passage)
            val score = predictor.predict(concatNsp(modelInput, passage))[0]
            window.content.map { Pair(it, score) }
        }
            .groupBy { it.first }
            .mapValues { it.value.map { pair -> pair.second }.average().toFloat() }

        scores.forEach { (token, score) -> invertedIndex.index(token, score, documentId) }

        log.info { "Content is indexed (id=$documentId)" }
        return documentId
    }

    private fun concatNsp(token: String, passage: String): String {
        return "$token $TOKEN_SEPARATOR $passage"
    }

    override fun preprocess(contents: List<String>): List<List<String>> = contents
        .map { it.lowercase() }
        .map { it.toTokens(stanfordCoreNlp) }
}
