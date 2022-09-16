package ru.itmo.stand.service.impl.bert_nsp

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
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


private typealias InvertedIndexType = ConcurrentHashMap<String, ConcurrentHashMap<String, Float>>

@Service
class DocumentBertNspService(
        bertNspTranslator: BertNspTranslator,
): DocumentService() {

    private val invertedIndexFile = Paths.get("${Params.BASE_PATH}/data/bert_nsp/inverted_index.bin").toFile()
    private lateinit var invertedIndex: InvertedIndexType

    private val predictor = Criteria.builder()
            .optApplication(Application.NLP.ANY)
            .setTypes(String::class.java, FloatArray::class.java)
            .optModelPath(Paths.get("${Params.BASE_PATH}/models/bert_nsp/bert.pt")) // search in local folder
            .optTranslator(bertNspTranslator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
            .newPredictor(bertNspTranslator)

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

    override val method: Method
        get() = Method.BERT_NSP

    override fun find(id: String): String? {
        return invertedIndex.toString() // TODO: add impl for finding by id
    }

    override fun search(query: String): List<String> {
/*        val tokens = preprocess(query)
        return tokens.asSequence()
                .mapNotNull { invertedIndex[it] }
                .reduce { m1, m2 ->
                    m2.forEach { (k, v) -> m1.merge(k, v) { v1, v2 -> v1.apply { v1 + v2 } } }
                    m1
                }
                .toList()
                .sortedByDescending { (_, score) -> score }
                .take(10)
                .map { (docId, _) -> docId }*/
        //TODO
        return emptyList();
    }

    /**
     * CLI command example: save -m BERT_NSP "Around 9 Million people live in London"
     */
    override fun save(content: String, withId: Boolean): String {
        //if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        //val (documentId, passage) = extractId(content)
        val passage = content;
        val documentId = "10";
        val tokens = preprocess(passage);


        val scoreByTokens = tokens
                .map { Pair(it, predictor.predict(concatNsp(it, passage))[0]) }


        scoreByTokens.forEach{ (token, score) ->
            invertedIndex.merge(token,
                ConcurrentHashMap(mapOf(documentId to score))) {
                v1, v2 -> v1.apply { if (!containsKey(documentId)) putAll(v2) }
            }
        }

        log.info("Content is indexed (id={})", documentId)
        return documentId
    }

    private fun concatNsp(token: String, passage: String): String {
        return "$token $TokenSeparator $passage"
    }

    /**
     * CLI command example: save-in-batch -m BERT_NSP --with-id data/collection.air-subset.tsv
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

    private fun preprocess(content: String): List<String> = preprocess(listOf(content))[0]

    private fun preprocess(contents: List<String>): List<List<String>> = contents
            .map { it.lowercase() }
            .map { it.toNgrams() }

    companion object {
        const val BATCH_SIZE_DOCUMENTS = 10
    }


}