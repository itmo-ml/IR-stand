package ru.itmo.stand.service.impl

import ai.djl.inference.Predictor
import ai.djl.translate.Translator
import edu.stanford.nlp.naturalli.ClauseSplitter.log
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore
import org.springframework.beans.factory.annotation.Autowired
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.bert.BertModelLoader
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.service.model.Format.JUST_QUERY
import ru.itmo.stand.service.model.Format.MS_MARCO
import ru.itmo.stand.util.createPath
import ru.itmo.stand.util.formatMrr
import ru.itmo.stand.util.toNgrams
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
        val invertedIndexPath = "$basePath/indexes/${method.name.lowercase()}/inverted_index.dat"
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

    abstract override val method: Method

    override fun find(id: String): String? {
        return invertedIndex.toString() // TODO: add impl for finding by id
    }

    override fun search(queries: File, format: Format): List<String> = when (format) {
        JUST_QUERY -> searchByQuery(queries.readLines().single())
        MS_MARCO -> {
            val queryByIdMap = getQueryByIdMap(queries)
            val outputLines = mutableListOf<String>()
            for ((queryId, query) in queryByIdMap) {
                val docsTopList = searchByQuery(query).mapIndexed { index, docId -> formatMrr(queryId, docId, index + 1) }
                outputLines.addAll(docsTopList)
            }
            val outputPath = "${standProperties.app.basePath}/outputs/${method.name.lowercase()}/queriesForMRR.tsv"
            File(outputPath).createPath().bufferedWriter()
                .use { output -> outputLines.forEach { line -> output.appendLine(line) } }
            listOf("See output in $outputPath")
        }
    }

    private fun searchByQuery(query: String): List<String> = preprocess(query).asSequence()
        .mapNotNull { invertedIndex[it] }
        .reduceOrNull { acc, scoreByDocIdMap ->
            scoreByDocIdMap.forEach { (docId, score) -> acc.merge(docId, score) { prev, new -> prev + new } }
            acc
        }
        ?.toList()
        ?.sortedByDescending { (_, score) -> score }
        ?.take(10)
        ?.map { (docId, _) -> docId } ?: emptyList()

    private fun getQueryByIdMap(queries: File): Map<Int, String> = queries.bufferedReader()
        .use { it.readLines() }
        .filter { it != "" }
        .map { it.split("\t") }
        .associate { it[0].toInt() to it[1] }

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
