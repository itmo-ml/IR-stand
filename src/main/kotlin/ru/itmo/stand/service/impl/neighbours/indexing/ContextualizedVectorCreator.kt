package ru.itmo.stand.service.impl.neighbours.indexing

import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.insert
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.storage.mongodb.model.neighbours.ContextualizedVector

@Service
class ContextualizedVectorCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(documents: Collection<Document>) {
        for ((index, document) in documents.withIndex()) {
            if (index % 1000 == 0) log.info("Vectors created: {}", index)
            create(document)
        }
    }

    fun create(document: Document) {
        val windows = preprocessingPipelineExecutor.execute(document.content)
        val contents = windows.map { it.convertContentToString() }.toTypedArray()
        val embeddingByMiddleTokenPairs = bertEmbeddingCalculator.calculate(contents)
            .zip(windows) { embedding, window -> window.middleToken to embedding }
        val vectors = embeddingByMiddleTokenPairs.map { (middleToken, embedding) ->
            ContextualizedVector(
                token = middleToken,
                documentId = document.id,
                vector = embedding,
            )
        }
        reactiveMongoTemplate.insert<ContextualizedVector>(vectors).subscribe()
    }
}
