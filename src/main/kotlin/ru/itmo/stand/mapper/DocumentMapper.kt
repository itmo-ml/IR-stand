package ru.itmo.stand

import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.model.Document

fun Document.toDto() = DocumentDto(
    content = content,
)

fun DocumentDto.toModel() = Document (
    id = null,
    content = content,
)
