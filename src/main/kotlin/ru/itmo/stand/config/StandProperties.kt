package ru.itmo.stand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = "stand")
data class StandProperties(
    val elasticsearch: ElasticsearchProperties,
) {
    data class ElasticsearchProperties(
        val hostAndPort: String,
    )
}
