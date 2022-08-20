package ru.itmo.stand.service.impl.custom

import ai.djl.Application
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
import edu.stanford.nlp.io.IOUtils.readObjectFromFile
import edu.stanford.nlp.io.IOUtils.writeObjectToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params.BASE_PATH
import ru.itmo.stand.content.model.ContentCustom
import ru.itmo.stand.content.repository.ContentCustomRepository
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.dot
import ru.itmo.stand.util.toNgrams
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private typealias InvertedIndexType = ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>

@Service
class DocumentCustomService(
    private val contentCustomRepository: ContentCustomRepository,
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

    override fun save(content: String, withId: Boolean): String {
        val (externalId, passage) = extractId(content, withId)
        val tokens = preprocess(passage)
        val documentId = contentCustomRepository.save(ContentCustom(content = passage)).id!!

        tokens.forEach { token ->
            invertedIndex.merge(
                token,
                ConcurrentHashMap(mapOf(documentId to predictor.computeScore(token, passage)))
            ) { v1, v2 -> v1.apply { if (!containsKey(documentId)) putAll(v2) } }
        }

        log.info("Content is indexed (mongodb id={})", documentId)
        return documentId
    }

    /**
     * CLI command example: save-in-batch -m CUSTOM --with-id data/collection.air-subset.tsv
     */
    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> = runBlocking(Dispatchers.Default) {
        log.info("Total size: ${contents.size}")
        contents.forEachIndexed { index, content ->
            if (index % 100 == 0) log.info("Indexing is started for $index passages")
            launch {
                save(content, withId)
            }
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
}
