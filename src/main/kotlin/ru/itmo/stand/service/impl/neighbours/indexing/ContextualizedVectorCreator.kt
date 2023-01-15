package ru.itmo.stand.service.impl.neighbours.indexing

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

    fun create(documents: Collection<Document>) {
        for (document in documents) {
            create(document)
        }
    }

    fun create(document: Document) {
        val windows = preprocessingPipelineExecutor.execute(document.content)
        val embeddingByMiddleTokenPairs =
            bertEmbeddingCalculator.calculate(windows.map { it.convertContentToString() }.toTypedArray())
                .zip(windows) { embedding, window -> Pair(window.middleToken, embedding) }
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
