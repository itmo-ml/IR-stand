package ru.itmo.stand.service

import ru.itmo.stand.config.Method

interface DocumentService {

    val method: Method

    fun find(id: String): String?

    fun search(query: String): List<String>

    fun save(content: String): String

    fun saveInBatch(contents: List<String>): List<String>

    fun getFootprint(): String?

}
