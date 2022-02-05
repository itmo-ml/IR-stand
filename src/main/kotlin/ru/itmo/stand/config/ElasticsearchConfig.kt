package ru.itmo.stand.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration

@Configuration
class ElasticsearchConfig : AbstractElasticsearchConfiguration() {

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo("localhost:9200") //TODO: move to config
            .build()
        return RestClients.create(clientConfiguration).rest()
    }

}
