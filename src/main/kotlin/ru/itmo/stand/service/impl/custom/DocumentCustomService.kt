package ru.itmo.stand.service.impl.custom

import ai.djl.Application
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
import java.nio.file.Paths
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.content.model.ContentCustom
import ru.itmo.stand.content.repository.ContentCustomRepository
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.dot
import ru.itmo.stand.util.toNgrams
import java.util.concurrent.ConcurrentHashMap

@Service
class DocumentCustomService(
    private val contentCustomRepository: ContentCustomRepository,
    private val translator: Translator<String, FloatArray>,
) : DocumentService() {

    private val invertedIndex = ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>()
    private val model: ZooModel<*, *> = Criteria.builder()
        .optApplication(Application.NLP.TEXT_EMBEDDING)
        .setTypes(String::class.java, FloatArray::class.java)
        .optModelPath(Paths.get("data/pytorch/bertqa/bert.pt")) // search in local folder
        .optTranslator(translator)
        .optProgress(ProgressBar())
        .build()
        .loadModel()

    override val method: Method
        get() = Method.CUSTOM

    override fun find(id: String): String? {
        TODO("Not yet implemented")
    }

    override fun search(query: String): List<String> {
        println(computeScore("How many people live in London?", "Around 9 Million people live in London"))
        println(computeScore("How many people live in London?", "London is known for its financial district"))
        return emptyList()
    }

    override fun save(content: String, withId: Boolean): String {
        val (externalId, passage) = extractId(content, withId)
        val tokens = preprocess(listOf(passage))
        val documentId = contentCustomRepository.save(ContentCustom(content = passage)).id!!

        tokens.forEach { token ->
            invertedIndex.merge(token, ConcurrentHashMap(mapOf(documentId to computeScore(token, passage)))) { _, v ->
                v.apply { computeIfAbsent(documentId) { computeScore(token, passage) } }
            }
        }

        return invertedIndex.toString()
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        TODO("Not yet implemented")
    }

    private fun preprocess(contents: List<String>): List<String> = contents.flatMap { it.toNgrams() }

    private fun computeScore(token: String, content: String): Double = model.newPredictor(translator).use {
        val tokenEmbedding = it.predict(token)
        val contentEmbedding = it.predict(content)
        tokenEmbedding dot contentEmbedding
    }
}
