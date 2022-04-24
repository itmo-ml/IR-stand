package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.Arrays

@Repository
class DocumentRepository(
    private val redisTemplate: RedisTemplate<String, Array<Double>>
) {

    private val ops = redisTemplate.opsForValue()

    public fun saveDoc(docId: String, vector: Array<Double>) {
        ops.set(docId, vector)
    }

    public fun getDoc(docId: String): Array<Double>? {
        return ops.get(docId)
    }
}