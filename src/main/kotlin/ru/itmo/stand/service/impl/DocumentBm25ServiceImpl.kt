package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.itmo.stand.dto.DocumentBm25Dto
import ru.itmo.stand.repository.DocumentBm25Repository
import ru.itmo.stand.service.DocumentBm25Service
import ru.itmo.stand.toDto
import ru.itmo.stand.toModel

@Service
class DocumentBm25ServiceImpl(
    private val documentBm25Repository: DocumentBm25Repository,
    private val stanfordCoreNlp: StanfordCoreNLP,
) : DocumentBm25Service {

    override fun find(id: String): DocumentBm25Dto? = documentBm25Repository.findByIdOrNull(id)?.toDto()

    override fun search(query: String): List<String> {
        val processedQuery = preprocess(query)
        return documentBm25Repository.findByContent(processedQuery)
            .map { it.id ?: throwDocIdNotFoundEx() }
    }

    override fun save(dto: DocumentBm25Dto): String {
        val model = dto.copy(content = preprocess(dto.content)).toModel()
        return documentBm25Repository.save(model).id
            ?: throwDocIdNotFoundEx()
    }

    override fun saveInBatch(dtoList: List<DocumentBm25Dto>): List<String> {
        val models = dtoList.map { it.copy(content = preprocess(it.content)) }
            .map { it.toModel() }
        return documentBm25Repository.saveAll(models)
            .map { it.id ?: throwDocIdNotFoundEx() }
    }

    //TODO: split to RAM/HDD
    override fun getFootprint(): String? {
        return RestTemplate().getForObject(
            "http://localhost:9200/_cat/indices/document?h=store.size", //TODO move to config
            String::class.java
        )
    }

    private fun preprocess(text: String) =
        stanfordCoreNlp.processToCoreDocument(text) // TODO: move to processor
            .tokens()
            .joinToString(" ") { it.lemma() }

    private fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")


}
