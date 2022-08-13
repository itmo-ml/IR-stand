package ru.itmo.stand.content.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.itmo.stand.content.model.ContentCustom

interface ContentCustomRepository: MongoRepository<ContentCustom, String>
