package ru.itmo.stand.storage.mongodb.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.itmo.stand.storage.mongodb.model.ContentSnrm

interface ContentSnrmRepository : MongoRepository<ContentSnrm, String> {

    fun findByIndexId(indexId: String): ContentSnrm?
}
