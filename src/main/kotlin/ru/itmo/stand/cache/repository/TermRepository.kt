package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TermRepository(
    redisTemplate: RedisTemplate<String, Any>,
) {
    private val hashOperations = redisTemplate.opsForHash<Float, String>()
    private val mapName = "term"

    fun saveTerm(key: Float, value: String) {
        hashOperations.put(mapName, key, value);
    }

    fun saveTerms(terms: MutableMap<Float, String>) {
        hashOperations.putAll(mapName, terms);
    }
    
    fun getTerm(key: Float): String? {
        return hashOperations.get(mapName, key)
    }
}
