package ru.itmo.stand.storage.mongodb.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import ru.itmo.stand.storage.mongodb.model.ContentSnrm

interface ContentSnrmRepository : ReactiveMongoRepository<ContentSnrm, String> {

    fun findByIndexId(indexId: String): Mono<ContentSnrm>
}
