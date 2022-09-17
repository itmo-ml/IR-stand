package ru.itmo.stand.service.impl

import ai.djl.Application
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
import edu.stanford.nlp.io.IOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.dot
import ru.itmo.stand.util.toNgrams
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.withLock

typealias InvertedIndexType = ConcurrentHashMap<String, ConcurrentHashMap<String, Float>>

abstract class BaseBertService(
        val translator: Translator<String, FloatArray>,
) : DocumentService() {

    protected lateinit var predictor: Predictor<String, FloatArray>
    protected abstract var modelName: String

    private val invertedIndexFile = Paths.get("${Params.BASE_PATH}/data/$modelName/inverted_index.bin").toFile()
    protected lateinit var invertedIndex: InvertedIndexType
    private val lock = ReentrantLock()

    //model only need during indexing, no need to load it for search
    protected fun ensureModelLoaded() {

        if (!this::predictor.isInitialized) {
            lock.withLock {
                if (!this::predictor.isInitialized) {
                    try {
                        this.predictor = Criteria.builder()
                                .optApplication(Application.NLP.TEXT_EMBEDDING)
                                .setTypes(String::class.java, FloatArray::class.java)
                                .optModelPath(Paths.get("${Params.BASE_PATH}/models/$modelName/bert.pt")) // search in local folder
                                .optTranslator(translator)
                                .optProgress(ProgressBar())
                                .build()
                                .loadModel()
                                .newPredictor(translator)
                    } catch (ex: Exception) {
                        log.error("Failed to load custom bert model", ex)
                        throw ex;
                    }
                }
            }
        }
    }

    @PostConstruct
    private fun readInvertedIndex() {
        invertedIndexFile.parentFile.mkdirs()
        invertedIndex = runCatching { IOUtils.readObjectFromFile<InvertedIndexType>(invertedIndexFile) }
                .getOrDefault(InvertedIndexType())
    }

    @PreDestroy
    private fun writeInvertedIndex() {
        IOUtils.writeObjectToFile(invertedIndex, invertedIndexFile)
        predictor.close()
    }

    abstract override val method: Method

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

    abstract override fun save(content: String, withId: Boolean): String

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
        Collections.emptyList()
    }

    protected fun preprocess(content: String): List<String> = preprocess(listOf(content))[0]

    protected fun preprocess(contents: List<String>): List<List<String>> = contents
            .map { it.lowercase() }
            .map { it.toNgrams() }


    companion object {
        const val BATCH_SIZE_DOCUMENTS = 10
    }
}