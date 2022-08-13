package ru.itmo.stand.content.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ContentCustom(
    @Id
    val id: String? = null,
    val content: String,
)
