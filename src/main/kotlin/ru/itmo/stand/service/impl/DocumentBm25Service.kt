package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.index.model.DocumentBm25
import ru.itmo.stand.index.repository.DocumentBm25Repository
import ru.itmo.stand.service.DocumentService

@Service
class DocumentBm25Service(
    private val documentBm25Repository: DocumentBm25Repository,
    private val stanfordCoreNlp: StanfordCoreNLP,
) : DocumentService() {

    override val method: Method
        get() = Method.BM25

    override fun find(id: String): String? = documentBm25Repository.findByIdOrNull(id)?.content

    override fun search(query: String): List<String> {
        val processedQuery = preprocess(query)
        return documentBm25Repository.findByContent(processedQuery)
            .map { it.id ?: throwDocIdNotFoundEx() }
    }

    override fun save(content: String, withId: Boolean): String {
        val (externalId, passage) = extractId(content, withId);
        val processedModel = DocumentBm25(content = content, representation = preprocess(passage), externalId = externalId)
        return documentBm25Repository.save(processedModel).id ?: throwDocIdNotFoundEx()
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        val processedModels = contents.map {
            val (externalId, passage) = extractId(it, withId);
            DocumentBm25(content = it, representation = preprocess(passage), externalId = externalId)
        }
        return documentBm25Repository.saveAll(processedModels).map { it.id ?: throwDocIdNotFoundEx() }
    }

    private fun preprocess(content: String) =
        stanfordCoreNlp.processToCoreDocument(content)
            .tokens()
            .joinToString(" ") { it.lemma() }
}
