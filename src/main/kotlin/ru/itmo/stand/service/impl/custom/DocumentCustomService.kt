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
import ru.itmo.stand.config.Params.BASE_PATH
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.dot
import ru.itmo.stand.util.toNgrams
import java.nio.file.Paths
import java.util.Collections.emptyList
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private typealias InvertedIndexType = ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>

@Service
class DocumentCustomService(
    customTranslator: Translator<String, FloatArray>,
) : DocumentService() {

    private val predictor = Criteria.builder()
        .optApplication(Application.NLP.TEXT_EMBEDDING)
        .setTypes(String::class.java, FloatArray::class.java)
        .optModelPath(Paths.get("$BASE_PATH/models/custom/bert.pt")) // search in local folder
        .optTranslator(customTranslator)
        .optProgress(ProgressBar())
        .build()
        .loadModel()
        .newPredictor(customTranslator)

    private val invertedIndexFile = Paths.get("$BASE_PATH/data/custom/inverted_index.bin").toFile()
    private lateinit var invertedIndex: InvertedIndexType

    @PostConstruct
    private fun readInvertedIndex() {
        invertedIndexFile.parentFile.mkdirs()
        invertedIndex = runCatching { readObjectFromFile<InvertedIndexType>(invertedIndexFile) }
            .getOrDefault(InvertedIndexType())
    }

    @PreDestroy
    private fun writeInvertedIndex() {
        writeObjectToFile(invertedIndex, invertedIndexFile)
        predictor.close()
    }

    override val method: Method
        get() = Method.CUSTOM

    override fun find(id: String): String? {
        return invertedIndex.toString() // TODO: add impl for finding by id
    }

    override fun search(query: String): List<String> {
        val tokens = preprocess(query)
        return tokens.asSequence()
            .mapNotNull { invertedIndex[it] }
            .reduce { m1, m2 ->
                m2.forEach { (k, v) -> m1.merge(k, v) { v1, v2 -> v1.apply { v1 + v2 } } }
                m1
            }
            .toList()
            .sortedByDescending { (_, score) -> score }
            .take(10)
            .map { (docId, _) -> docId }
    }

    /**
     * CLI command example: save -m CUSTOM "Around 9 Million people live in London"
     */
    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (documentId, passage) = extractId(content) { it }
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

    /**
     * CLI command example: save-in-batch -m CUSTOM --with-id data/collection.air-subset.tsv
     */
    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> = runBlocking(Dispatchers.Default) {
        log.info("Total size: ${contents.size}")
        val channel = Channel<String>(BATCH_SIZE_DOCUMENTS)
        contents.asSequence()
            .onEachIndexed { index, _ -> if (index % 10 == 0) log.info("Indexing is started for $index passages") }
            .forEach { content ->
                channel.send(content)
                launch { save(channel.receive(), withId) }
            }
        emptyList()
    }

    private fun preprocess(content: String): List<String> = preprocess(listOf(content))[0]

    private fun preprocess(contents: List<String>): List<List<String>> = contents
        .map { it.lowercase() }
        .map { it.toNgrams() }

    private fun Predictor<String, FloatArray>.computeScore(token: String, content: String): Double {
        val tokenEmbedding = this.predict(token)
        val contentEmbedding = this.predict(content)
        return tokenEmbedding dot contentEmbedding
    }

    companion object {
        const val BATCH_SIZE_DOCUMENTS = 10
    }
}
