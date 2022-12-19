package ru.itmo.stand.service.impl.neighbours.indexing

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Service
import ru.itmo.stand.service.impl.neighbours.EmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.storage.mongodb.model.neighbours.ContextualizedVector

@Service
class ContextualizedVectorCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val embeddingCalculator: EmbeddingCalculator,
) {

    fun create(documents: Collection<Document>) {
        for (document in documents) {
            create(document)
        }
    }

    private fun create(document: Document) {
        preprocessingPipelineExecutor.execute(document.content).forEach { window ->
            val embedding = embeddingCalculator.calculate(window)
            reactiveMongoTemplate.insert(
                ContextualizedVector(
                    token = window.middleToken,
                    documentId = document.id,
                    vector = embedding
                )
            ).subscribe()
        }
    }
}
