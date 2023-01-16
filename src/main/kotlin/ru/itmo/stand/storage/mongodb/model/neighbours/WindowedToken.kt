package ru.itmo.stand.storage.mongodb.model.neighbours

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("WindowedToken")
data class WindowedToken(
    @Id
    val id: String? = null,
    @Indexed
    val token: String,
    @Indexed
    val documentId: String,
    val window: String,
)
