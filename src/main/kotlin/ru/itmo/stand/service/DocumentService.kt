package ru.itmo.stand.service

import ru.itmo.stand.service.model.Format
import java.io.File

interface DocumentService {
    fun find(id: String): String?
    fun search(queries: File, format: Format): List<String>
    fun save(content: String, withId: Boolean): String
    suspend fun saveInBatch(contents: File, withId: Boolean): List<String>
    fun getFootprint(): String
}
