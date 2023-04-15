package ru.itmo.stand.config

import io.weaviate.client.Config
import io.weaviate.client.WeaviateClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WeaviateStorageConfig {

    @Bean
    fun weaviateClient(): WeaviateClient {
        val config = Config("http", "localhost:8080")
        return WeaviateClient(config)
    }
}
