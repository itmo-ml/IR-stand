package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TokenCounterRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private val stringOps = redisTemplate.opsForValue()
    private val counterKey = "token_counter"

    public fun getNext(): Long? {

        //if key does not exist it will be auto-created
        return stringOps.increment(counterKey)
    }

}