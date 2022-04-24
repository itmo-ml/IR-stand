package ru.itmo.stand.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(value = "stand")
data class StandProperties(
    val elasticsearch: ElasticsearchProperties,
    val redis: RedisProperties
) {
    data class ElasticsearchProperties(
        val hostAndPort: String,
    )
    data class RedisProperties(
        val host: String,
        val port: Int
    )
}
