package ru.itmo.stand.service

import ru.itmo.stand.dto.DocumentDto

interface DocumentService {

    fun indexDocument(dto: DocumentDto): String

}
