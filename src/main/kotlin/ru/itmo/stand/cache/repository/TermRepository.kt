package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TermRepository(
    redisTemplate: RedisTemplate<String, Any>,
) {
    private val hashOperations = redisTemplate.opsForHash<Double, String>()
    private val mapName = "term"

    fun saveTerm(key: Double, value: String) {
        //TODO: can be optimized for batch insert using lua scripts
        hashOperations.put(mapName, key, value);
    }

    fun getTerm(key: Double): String? {
        return hashOperations.get(mapName, key)
    }
}
