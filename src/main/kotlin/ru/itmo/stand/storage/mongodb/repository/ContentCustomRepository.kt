package ru.itmo.stand.storage.mongodb.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import ru.itmo.stand.storage.mongodb.model.ContentCustom

interface ContentCustomRepository: ReactiveMongoRepository<ContentCustom, String>
