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
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.service.lucene.WindowedToken

class WindowedTokenCreatorTest {

    private val reactiveMongoTemplate = mockk<ReactiveMongoTemplate>()
    private val windowedTokenCreator = WindowedTokenCreator(
        preprocessingPipelineExecutor(),
        reactiveMongoTemplate,
    )

    @Test
    fun `should do to do`() {
        val content = "Definition of Central nervous system (CNS): " +
            "The central nervous system is that part of the nervous system that consists " +
            "of the brain and spinal cord."
        val windows = arrayOf(
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

        val flux = mockk<Flux<WindowedToken>>()
        every { flux.subscribe() } returns Disposable { }
        every {
            reactiveMongoTemplate.insert<WindowedToken>(any<List<WindowedToken>>())
        } returns flux

        windowedTokenCreator.create(Document("id", content))

        val slot = slot<List<WindowedToken>>()
        verify(exactly = 1) { reactiveMongoTemplate.insert<WindowedToken>(capture(slot)) }
        verify(exactly = 1) { flux.subscribe() }
        assertThat(slot.captured)
            .containsExactly(
                WindowedToken(token = "definition", documentId = "id", window = windows[0]),
                WindowedToken(token = "central", documentId = "id", window = windows[1]),
                WindowedToken(token = "nervous", documentId = "id", window = windows[2]),
                WindowedToken(token = "system", documentId = "id", window = windows[3]),
                WindowedToken(token = "cns", documentId = "id", window = windows[4]),
                WindowedToken(token = "central", documentId = "id", window = windows[5]),
                WindowedToken(token = "nervous", documentId = "id", window = windows[6]),
                WindowedToken(token = "system", documentId = "id", window = windows[7]),
                WindowedToken(token = "part", documentId = "id", window = windows[8]),
                WindowedToken(token = "nervous", documentId = "id", window = windows[9]),
                WindowedToken(token = "system", documentId = "id", window = windows[10]),
                WindowedToken(token = "consist", documentId = "id", window = windows[11]),
                WindowedToken(token = "brain", documentId = "id", window = windows[12]),
                WindowedToken(token = "spinal", documentId = "id", window = windows[13]),
                WindowedToken(token = "cord", documentId = "id", window = windows[14]),
            )
    }
}
