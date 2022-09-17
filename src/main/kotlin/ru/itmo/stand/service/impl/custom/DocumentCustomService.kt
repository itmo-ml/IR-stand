package ru.itmo.stand.service.impl.custom

import ai.djl.Application
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
import edu.stanford.nlp.io.IOUtils.readObjectFromFile
import edu.stanford.nlp.io.IOUtils.writeObjectToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params
import ru.itmo.stand.config.Params.BASE_PATH
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.impl.BaseBertService
import ru.itmo.stand.util.dot
import ru.itmo.stand.util.toNgrams
import java.nio.file.Paths
import java.util.Collections.emptyList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock

private typealias InvertedIndexType = ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>

@Service
class DocumentCustomService(
    val customTranslator: Translator<String, FloatArray>,
) : BaseBertService(customTranslator) {

    override val method: Method
        get() = Method.CUSTOM

    override var modelName = "custom";

    /**
     * CLI command example: save -m CUSTOM "Around 9 Million people live in London"
     */
    override fun save(content: String, withId: Boolean): String {
        ensureModelLoaded()
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
            invertedIndex.merge(
                token,
                ConcurrentHashMap(mapOf(documentId to (tokenVector dot passageVector)))
            ) { v1, v2 -> v1.apply { if (!containsKey(documentId)) putAll(v2) } }
        }

        log.info("Content is indexed (id={})", documentId)
        return documentId
    }

}
