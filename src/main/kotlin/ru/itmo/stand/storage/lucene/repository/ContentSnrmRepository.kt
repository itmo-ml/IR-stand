package ru.itmo.stand.storage.lucene.repository

import org.springframework.stereotype.Repository
import ru.itmo.stand.storage.lucene.model.ContentSnrm

@Repository
class ContentSnrmRepository {
    fun save(contentSnrm: ContentSnrm): ContentSnrm = TODO()
    fun findByIndexId(indexId: String): ContentSnrm? = TODO()
    fun saveAll(contents: List<ContentSnrm>): List<ContentSnrm> = TODO()
}
