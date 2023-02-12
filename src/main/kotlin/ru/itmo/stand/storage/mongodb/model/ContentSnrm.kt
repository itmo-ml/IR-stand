package ru.itmo.stand.storage.mongodb.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ContentSnrm(
    @Id
    val id: String? = null,
    @Indexed(unique = true)
    val indexId: String,
    val content: String,
)
