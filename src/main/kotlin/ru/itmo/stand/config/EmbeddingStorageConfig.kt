package ru.itmo.stand.config

import io.weaviate.client.Config
import io.weaviate.client.WeaviateClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.hnsw.ContextualizedEmbeddingInMemoryRepository
import ru.itmo.stand.storage.embedding.weaviate.ContextualizedEmbeddingWeaviateRepository

@Configuration
class EmbeddingStorageConfig {

    @Bean
    fun weaviateClient(): WeaviateClient {
        val config = Config("http", "localhost:8080")
        return WeaviateClient(config)
    }

    @Bean
    @ConditionalOnProperty(value = ["stand.app.neighbours-algorithm.embedding-storage"], havingValue = "WEAVIATE")
    fun contextualizedEmbeddingWeaviateRepository(client: WeaviateClient): ContextualizedEmbeddingRepository {
        return ContextualizedEmbeddingWeaviateRepository(client)
    }

    @Bean
    @ConditionalOnProperty(value = ["stand.app.neighbours-algorithm.embedding-storage"], havingValue = "IN_MEMORY")
    fun contextualizedEmbeddingInMemoryRepository(standProperties: StandProperties): ContextualizedEmbeddingRepository {
        return ContextualizedEmbeddingInMemoryRepository(standProperties)
    }
}