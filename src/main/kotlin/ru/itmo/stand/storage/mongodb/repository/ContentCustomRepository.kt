package ru.itmo.stand.storage.mongodb.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.itmo.stand.storage.mongodb.model.ContentCustom

interface ContentCustomRepository: MongoRepository<ContentCustom, String>
