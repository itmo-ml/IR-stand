package ru.itmo.stand.content.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.itmo.stand.content.model.ContentSnrm

interface ContentSnrmRepository : MongoRepository<ContentSnrm, String> {

    fun findByIndexId(indexId: String): ContentSnrm?
}
