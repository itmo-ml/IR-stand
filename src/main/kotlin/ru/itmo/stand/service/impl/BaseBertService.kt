package ru.itmo.stand.service.impl

import ai.djl.inference.Predictor
import ai.djl.translate.Translator
import edu.stanford.nlp.naturalli.ClauseSplitter.log
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore
import org.springframework.beans.factory.annotation.Autowired
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.bert.BertModelLoader
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.service.model.Format.JUST_QUERY
import ru.itmo.stand.service.model.Format.MS_MARCO
import ru.itmo.stand.util.createPath
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.toNgrams
import ru.itmo.stand.util.writeAsFileInMrrFormat
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

typealias KeyType = String
typealias ValueType = ConcurrentHashMap<String, Float>
typealias InvertedIndexType = MVMap<KeyType, ValueType>

abstract class BaseBertService(
    private val translator: Translator<String, FloatArray>,
) : DocumentService {

    @Autowired
    protected lateinit var standProperties: StandProperties

    @Autowired
    private lateinit var bertModelLoader: BertModelLoader

    private val invertedIndexStore by lazy {
        val basePath = standProperties.app.basePath
        val invertedIndexPath = "$basePath/indexes/${standProperties.app.method.name.lowercase()}/inverted_index.dat"
        File(invertedIndexPath).createPath()
        MVStore.open(invertedIndexPath)
    }
    protected val predictor: Predictor<String, FloatArray> by lazy {
        bertModelLoader.deprecatedModel().newPredictor(translator)
    }
    protected lateinit var invertedIndex: InvertedIndexType

    @PostConstruct
    private fun readInvertedIndex() {
        invertedIndex = runCatching { invertedIndexStore.openMap<KeyType, ValueType>("inverted_index") }
            .onFailure { log.error("Could not load inverted index", it) }
            .getOrThrow()
    }

    @PreDestroy
    private fun writeInvertedIndex() {
        invertedIndexStore.close()
    }

    override fun find(id: String): String? {
        return invertedIndex.toString() // TODO: add impl for finding by id
    }

    override fun search(queries: File, format: Format): List<String> = when (format) {
        JUST_QUERY -> searchByQuery(queries.readLines().single())
        MS_MARCO -> {
            val outputPath = "${standProperties.app.basePath}/" +
                "outputs/${standProperties.app.method.name.lowercase()}/resultInMrrFormat.tsv"
            writeAsFileInMrrFormat(queries, outputPath) { query -> searchByQuery(query) }
            listOf("See output in $outputPath")
        }
    }

    private fun searchByQuery(query: String): List<String> = preprocess(query).asSequence()
        .mapNotNull { invertedIndex[it] }
        .fold(HashMap<String, Float>()) { acc, scoreByDocIdMap ->
            scoreByDocIdMap.forEach { (docId, score) -> acc.merge(docId, score) { prev, new -> prev + new } }
            acc
        }.entries
        .sortedByDescending { (_, score) -> score }
        .take(10)
        .map { (docId, _) -> docId }

    /**
     * CLI command example: save -m CUSTOM "Around 9 Million people live in London"
     */

    abstract override fun save(content: String, withId: Boolean): String

    /**
     * CLI command example: save-in-batch -m CUSTOM --with-id data/collection.air-subset.tsv
     */
    override suspend fun saveInBatch(contents: File, withId: Boolean): List<String> = coroutineScope {
        val channel = Channel<String>(BATCH_SIZE_DOCUMENTS)
        contents.lineSequence()
            .onEachIndexed { index, _ -> if (index % 10 == 0) log.info("Indexing is started for $index passages") }
            .forEach { content ->
                channel.send(content)
                launch { save(channel.receive(), withId) }
            }
        Collections.emptyList()
    }

    override fun getFootprint(): String {
        TODO("Not yet implemented")
    }

    protected fun InvertedIndexType.index(token: String, score: Float, documentId: String) {
        this.merge(token, ConcurrentHashMap(mapOf(documentId to score))) { v1, v2 ->
            v1.apply { if (!containsKey(documentId)) putAll(v2) }
        }
    }

    protected fun preprocess(content: String): List<String> = preprocess(listOf(content))[0]

    protected open fun preprocess(contents: List<String>): List<List<String>> = contents
        .map { it.lowercase() }
        .map { it.toNgrams() }

    companion object {
        const val BATCH_SIZE_DOCUMENTS = 10
    }
}
