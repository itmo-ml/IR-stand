package ru.itmo.stand.storage.lucene.repository

import org.springframework.stereotype.Repository
import ru.itmo.stand.storage.lucene.model.DocumentSnrm

@Repository
class DocumentSnrmRepository {
    fun findAllByRepresentation(representation: String): List<DocumentSnrm> = TODO()
    fun save(documentSnrm: DocumentSnrm): DocumentSnrm = TODO()
    fun saveAll(entities: List<DocumentSnrm>): List<DocumentSnrm> = TODO()
}
