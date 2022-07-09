package ru.itmo.stand.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration

@Configuration
class ElasticsearchConfig(
    private val standProperties: StandProperties
) : AbstractElasticsearchConfiguration() {

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo(standProperties.elasticsearch.hostAndPort)
            .withSocketTimeout(30_000)
            .build()
        return RestClients.create(clientConfiguration).rest()
    }

}
