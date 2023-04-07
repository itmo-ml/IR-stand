package ru.itmo.stand.storage.embedding

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Service

@Service
class ContextualizedEmbeddingModelInitializer(
    private val embeddingStorageClient: EmbeddingStorageClient,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val result = embeddingStorageClient.ensureContextualizedEmbeddingModel()
        if (result.hasErrors()) {
            log.error("Errors during ContextualizedEmbeddingModel ensuring. Errors: ${result.error}")
        } else {
            log.info("ContextualizedEmbeddingModel ensured. Result: ${result.result}")
        }
    }
}
