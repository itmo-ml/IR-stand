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
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params.BASE_PATH
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.toNgrams
import java.nio.file.Paths
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

typealias InvertedIndexType = ConcurrentHashMap<String, ConcurrentHashMap<String, Float>>

abstract class BaseBertService(
    private val translator: Translator<String, FloatArray>,
) : DocumentService() {

    private val invertedIndexFile by lazy {
        Paths.get("$BASE_PATH/data/${method.name.lowercase()}/inverted_index.bin").toFile()
    }
    protected val predictor: Predictor<String, FloatArray> by lazy {
        Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(String::class.java, FloatArray::class.java)
            .optModelPath(Paths.get("$BASE_PATH/models/${method.name.lowercase()}/bert.pt")) // search in local folder
            .optTranslator(translator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
            .newPredictor(translator)
    }
    protected lateinit var invertedIndex: InvertedIndexType

    @PostConstruct
    private fun readInvertedIndex() {
        invertedIndexFile.parentFile.mkdirs()
        invertedIndex = runCatching { IOUtils.readObjectFromFile<InvertedIndexType>(invertedIndexFile) }
            .getOrDefault(InvertedIndexType())
    }

    @PreDestroy
    private fun writeInvertedIndex() {
        IOUtils.writeObjectToFile(invertedIndex, invertedIndexFile)
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

    protected open fun preprocess(contents: List<String>): List<List<String>> = contents
        .map { it.lowercase() }
        .map { it.toNgrams() }

    companion object {
        const val BATCH_SIZE_DOCUMENTS = 10
    }
}
