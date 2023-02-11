package ru.itmo.stand.service.lucene

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

data class WindowedToken(
    val token: String,
    val documentId: String,
    val window: String,
)
