package ru.itmo.stand.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate


@Configuration
class RedisConfig(
    private val standProperties: StandProperties,
) {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory = LettuceConnectionFactory(
        RedisStandaloneConfiguration(standProperties.redis.host, standProperties.redis.port)
    )

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(redisConnectionFactory)
        return template
    }
}
