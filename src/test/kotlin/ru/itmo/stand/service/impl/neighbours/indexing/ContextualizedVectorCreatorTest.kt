package ru.itmo.stand.service.impl.neighbours.indexing

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.insert
import reactor.core.Disposable
import reactor.core.publisher.Flux
import ru.itmo.stand.fixtures.bertEmbeddingCalculator
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.storage.mongodb.model.neighbours.ContextualizedVector

class ContextualizedVectorCreatorTest {

    private val reactiveMongoTemplate = mockk<ReactiveMongoTemplate>()
    private val bertEmbeddingCalculator = bertEmbeddingCalculator()
    private val contextualizedVectorCreator = ContextualizedVectorCreator(
        preprocessingPipelineExecutor(),
        bertEmbeddingCalculator,
        reactiveMongoTemplate,
    )

    @Test
    fun `should do to do`() {
        val content = "Definition of Central nervous system (CNS): " +
            "The central nervous system is that part of the nervous system that consists " +
            "of the brain and spinal cord."
        val embeddings = bertEmbeddingCalculator.calculate(
            arrayOf(
                "definition central nervous",
                "definition central nervous system",
                "definition central nervous system cns",
                "central nervous system cns central",
                "nervous system cns central nervous",
                "system cns central nervous system",
                "cns central nervous system part",
                "central nervous system part nervous",
                "nervous system part nervous system",
                "system part nervous system consist",
                "part nervous system consist brain",
                "nervous system consist brain spinal",
                "system consist brain spinal cord",
                "consist brain spinal cord",
                "brain spinal cord",
            )
        )
        val flux = mockk<Flux<ContextualizedVector>>()
        every { flux.subscribe() } returns Disposable { }
        every {
            reactiveMongoTemplate.insert<ContextualizedVector>(any<List<ContextualizedVector>>())
        } returns flux

        contextualizedVectorCreator.create(Document("id", content))

        val slot = slot<List<ContextualizedVector>>()
        verify(exactly = 1) { reactiveMongoTemplate.insert<ContextualizedVector>(capture(slot)) }
        verify(exactly = 1) { flux.subscribe() }
        assertThat(slot.captured)
            .containsExactly(
                ContextualizedVector(token = "definition", documentId = "id", vector = embeddings[0]),
                ContextualizedVector(token = "central", documentId = "id", vector = embeddings[1]),
                ContextualizedVector(token = "nervous", documentId = "id", vector = embeddings[2]),
                ContextualizedVector(token = "system", documentId = "id", vector = embeddings[3]),
                ContextualizedVector(token = "cns", documentId = "id", vector = embeddings[4]),
                ContextualizedVector(token = "central", documentId = "id", vector = embeddings[5]),
                ContextualizedVector(token = "nervous", documentId = "id", vector = embeddings[6]),
                ContextualizedVector(token = "system", documentId = "id", vector = embeddings[7]),
                ContextualizedVector(token = "part", documentId = "id", vector = embeddings[8]),
                ContextualizedVector(token = "nervous", documentId = "id", vector = embeddings[9]),
                ContextualizedVector(token = "system", documentId = "id", vector = embeddings[10]),
                ContextualizedVector(token = "consist", documentId = "id", vector = embeddings[11]),
                ContextualizedVector(token = "brain", documentId = "id", vector = embeddings[12]),
                ContextualizedVector(token = "spinal", documentId = "id", vector = embeddings[13]),
                ContextualizedVector(token = "cord", documentId = "id", vector = embeddings[14]),
            )
    }
}
