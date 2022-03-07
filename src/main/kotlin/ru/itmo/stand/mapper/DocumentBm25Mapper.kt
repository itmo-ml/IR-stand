package ru.itmo.stand

import ru.itmo.stand.dto.DocumentBm25Dto
import ru.itmo.stand.model.DocumentBm25

fun DocumentBm25.toDto() = DocumentBm25Dto(
    content = content,
)

fun DocumentBm25Dto.toModel() = DocumentBm25 (
    id = null,
    content = content,
)
