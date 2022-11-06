package ru.itmo.stand.service.impl

import ai.djl.Application
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
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
import ru.itmo.stand.util.toNgrams
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

typealias KeyType = String
typealias ValueType = ConcurrentHashMap<String, Float>
typealias InvertedIndexType = MVMap<KeyType, ValueType>

abstract class BaseBertService(
    private val translator: Translator<String, FloatArray>,
) : DocumentService() {

    @Autowired
    protected lateinit var standProperties: StandProperties

    private val invertedIndexStore by lazy {
        val basePath = standProperties.app.basePath
        val invertedIndexPath = "$basePath/indexes/${method.name.lowercase()}/inverted_index.dat"
        File(invertedIndexPath).parentFile.mkdirs()
        MVStore.open(invertedIndexPath)
    }
    protected val predictor: Predictor<String, FloatArray> by lazy {
        val basePath = standProperties.app.basePath
        Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(String::class.java, FloatArray::class.java)
            .optModelUrls("$basePath/models/${method.name.lowercase()}/distilbert.pt")
            .optTranslator(translator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
            .newPredictor(translator)
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

    fun getQueryByIdMap(): Map<Int, String> = File("collections/queries.air-subset.tsv").bufferedReader()
        .use { it.readLines() }
        .filter { it != "" }
        .map { it.split("\t") }
        .associate { it[0].toInt() to it[1] }

    override fun search(query: String): List<String> {
        //val basePath = standProperties.app.basePath
        var getQueryByIdMap = getQueryByIdMap()
        var docQueryPosForMRR: MutableList<String> = mutableListOf()
        for ((queryId, query) in getQueryByIdMap) {
            val tokens = preprocess(query)
            val cdf = (tokens //.asSequence()
                .mapNotNull { invertedIndex[it] }
                .takeIf { it.isNotEmpty() }
                ?.reduce { m1, m2 ->
                    m2.forEach { (k, v) -> m1.merge(k, v) { v1, v2 -> v1.apply { v1 + v2 } } }
                    m1
                }
                ?.toList()
                ?.sortedByDescending { (_, score) -> score }
                ?.take(10)
                ?.map { (docId, _) -> docId })
                //?.mapIndexed { (index, docId) -> formatMrr(docId, queryId, index)})
                ?: emptyList()

            //docQueryPosForMRR.addALL(cdf)

            var counterZ = 0
            for (i in cdf) {
                docQueryPosForMRR.add(queryId.toString().replace("[", "").replace("]", "") + "\t"+ "\t" + i.toString() + "\t" +counterZ)
                counterZ +=1
            }
          }

        //File("$basePath/collections/${method.name.lowercase()}/queriesForMRR.tsv").bufferedWriter().use { out ->
        File("collections/queriesForMRR.tsv").bufferedWriter().use { out ->

            for (i in docQueryPosForMRR) {
                out.appendLine(i)
            //out.appendLine("\n")
            }
        }

        return emptyList()

    }

//search otladka i proverka
//    override fun search(query: String): List<String> {
//        val tokens = preprocess(query)
//        return tokens.asSequence()
//            .mapNotNull { invertedIndex[it] }
//            .reduce { m1, m2 ->
//                m2.forEach { (k, v) -> m1.merge(k, v) { v1, v2 -> v1.apply { v1 + v2 } } }
//                m1
//            }
//            .toList()
//            .sortedByDescending { (_, score) -> score }
//            .take(10)
//            .map { (docId, _) -> docId }
//    }

    fun formatMrr(docId: Int, queryId: Int, index: Int): List<String>{
        var docQueryPosForMRR: MutableList<String> = mutableListOf()
         docQueryPosForMRR.add(
            queryId.toString().replace("[", "")
                .replace("]", "") +
                    "\t" + "\t"
                    + docId.toString() +
                    "\t" + index);

         return docQueryPosForMRR

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
