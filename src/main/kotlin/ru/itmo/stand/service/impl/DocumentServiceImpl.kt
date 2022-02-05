package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.stereotype.Service
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.repository.DocumentRepository
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.toModel

@Service
class DocumentServiceImpl(
    private val documentRepository: DocumentRepository,
    private val stanfordCoreNlp: StanfordCoreNLP,
) : DocumentService {

    override fun indexDocument(dto: DocumentDto): String {

        val document: CoreDocument = stanfordCoreNlp.processToCoreDocument(dto.content) // TODO: move to processor
        val lemmas = document.tokens().joinToString(" ") { it.lemma() }

        return documentRepository.save(dto.copy(content = lemmas).toModel()).id
            ?: throw IllegalStateException("Document id must not be null.")
    }

}
