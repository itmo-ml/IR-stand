package ru.itmo.stand.service

import ru.itmo.stand.dto.DocumentBm25Dto

interface DocumentBm25Service {

    fun find(id: String): DocumentBm25Dto?

    fun search(query: String): List<String>

    fun save(dto: DocumentBm25Dto): String

    fun saveInBatch(dtoList: List<DocumentBm25Dto>): List<String>

    fun getFootprint(): String?

}
