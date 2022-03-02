package ru.itmo.stand.service

import ru.itmo.stand.dto.DocumentDto

interface DocumentService {

    fun find(id: String): DocumentDto?

    fun search(query: String): List<String>

    fun save(dto: DocumentDto): String

    fun saveInBatch(dtoList: List<DocumentDto>): List<String>

    fun getFootprint(): String?

}
