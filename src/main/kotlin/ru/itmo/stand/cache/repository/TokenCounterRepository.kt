package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TokenCounterRepository(
    redisTemplate: RedisTemplate<String, Any>
) {
    private val valueOperations = redisTemplate.opsForValue()
    private val counterKey = "token_counter"

    fun getNext(): Long {
        //if key does not exist it will be auto-created
        return valueOperations.increment(counterKey) ?: throw IllegalStateException("Next value can't be null")
    }
}
