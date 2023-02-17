package ru.itmo.stand.service.impl.neighbours.indexing

import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.insert
import org.springframework.stereotype.Service
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.lucene.LuceneDocument
import ru.itmo.stand.service.lucene.LuceneService
import ru.itmo.stand.service.model.Document

@Service
class WindowedTokenCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val luceneService: LuceneService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(documents: Sequence<Document>) {
        for ((index, document) in documents.withIndex()) {
            if (index % 1000 == 0) log.info("Vectors created: {}", index)
            create(document)
        }
        luceneService.completeIndexing()
    }

    fun create(document: Document) {
        val windows = preprocessingPipelineExecutor.execute(document.content)
        val windowedTokens = windows.map {
            LuceneDocument(
                groupKey = it.middleToken,
                documentId = document.id,
                content = it.convertContentToString(),
            )
        }
        luceneService.saveInBatch(windowedTokens)
    }
}
