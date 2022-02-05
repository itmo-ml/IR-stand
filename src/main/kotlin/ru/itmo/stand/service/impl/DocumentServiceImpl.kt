package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.repository.DocumentRepository
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.toDto
import ru.itmo.stand.toModel

@Service
class DocumentServiceImpl(
    private val documentRepository: DocumentRepository,
    private val stanfordCoreNlp: StanfordCoreNLP,
) : DocumentService {

    override fun find(id: String): DocumentDto? = documentRepository.findByIdOrNull(id)?.toDto()

    override fun search(query: String): List<String> {
        val processedQuery = preprocess(query)
        return documentRepository.findByContent(processedQuery)
            .map { it.id ?: throw IllegalStateException("Document id must not be null.") }
    }

    override fun index(dto: DocumentDto): String {
        val preprocessedContent = preprocess(dto.content)
        return documentRepository.save(dto.copy(content = preprocessedContent).toModel()).id
            ?: throw IllegalStateException("Document id must not be null.")
    }

    private fun preprocess(text: String) =
        stanfordCoreNlp.processToCoreDocument(text) // TODO: move to processor
            .tokens()
            .joinToString(" ") { it.lemma() }


}
