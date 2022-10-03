package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.index.model.DocumentBm25
import ru.itmo.stand.index.repository.DocumentBm25Repository
import ru.itmo.stand.service.DocumentService

@Profile("!standalone")
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
        return documentBm25Repository.findByRepresentation(processedQuery)
            .map { it.id ?: throwDocIdNotFoundEx() }
    }

    override fun save(content: String, withId: Boolean): String {
        val (externalId, passage) = extractId(content, withId);
        val processedModel = DocumentBm25(content = passage, representation = preprocess(passage), externalId = externalId)
        return documentBm25Repository.save(processedModel).id ?: throwDocIdNotFoundEx()
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        log.info("Total size: ${contents.size}")
        for (chunk in contents.chunked(1000)) {
            val processedModels = chunk.map {
                val (externalId, passage) = extractId(it, withId);
                DocumentBm25(content = it, representation = preprocess(passage), externalId = externalId)
            }
            documentBm25Repository.saveAll(processedModels)
            log.info("Index now holds ${documentBm25Repository.count()} documents")
        }

        return emptyList()
    }

    private fun preprocess(content: String) =
        stanfordCoreNlp.processToCoreDocument(content)
            .tokens()
            .joinToString(" ") { it.lemma() }
}
