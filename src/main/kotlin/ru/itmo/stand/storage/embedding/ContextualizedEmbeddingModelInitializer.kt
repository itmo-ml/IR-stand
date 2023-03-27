package ru.itmo.stand.storage.embedding

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class ContextualizedEmbeddingModelInitializer(
    private val embeddingStorageClient: EmbeddingStorageClient,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun ensureContextualizedEmbeddingModel() {
        val result = embeddingStorageClient.ensureContextualizedEmbeddingModel()
        if (result.hasErrors()) {
            log.error("Errors during ContextualizedEmbeddingModel ensuring. Errors: ${result.error}")
        } else {
            log.info("ContextualizedEmbeddingModel ensured. Result: ${result.result}")
        }
    }
}
