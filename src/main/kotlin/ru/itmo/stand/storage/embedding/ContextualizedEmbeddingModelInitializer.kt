package ru.itmo.stand.storage.embedding

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!standalone")
class ContextualizedEmbeddingModelInitializer(
    private val embeddingStorage: IEmbeddingStorage,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val result = embeddingStorage.initialize()
        if (result) {
            log.error("Errors during ContextualizedEmbeddingModel ensuring.")
        } else {
            log.info("ContextualizedEmbeddingModel ensured. ")
        }
    }
}
