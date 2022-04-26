package ru.itmo.stand.cache.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class DocumentVectorRepository(
    redisTemplate: RedisTemplate<String, Any>
) {
    private val hashOperations = redisTemplate.opsForHash<String, FloatArray>()
    private val mapName = "document_vector_snrm"

    fun saveDoc(docId: String, vector: FloatArray) {
        hashOperations.put(mapName, docId, vector)
    }

    fun saveDocs(documents: Map<String, FloatArray>) {
        hashOperations.putAll(mapName, documents)
    }

    fun getDoc(docId: String): FloatArray? = hashOperations.get(mapName, docId)
}
