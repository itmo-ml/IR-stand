package ru.itmo.stand.service

import ru.itmo.stand.config.Method
import java.io.File

interface DocumentService {

    val method: Method

    fun find(id: String): String?

    fun search(queries: File, format: Format): List<String>

    fun save(content: String, withId: Boolean): String

    fun saveInBatch(contents: List<String>, withId: Boolean): List<String>

    fun getFootprint(): String

    fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")

    fun extractId(content: String): Pair<String, String> = extractId(content) { it }

    fun extractId(content: String, withId: Boolean): Pair<Long?, String> = if (withId)
        extractId(content) { it.toLong() }
    else Pair(null, content)

    private fun <T> extractId(content: String, idTransform: (String) -> T): Pair<T, String> {
        val idAndPassage = content.split("\t")
        if (idAndPassage.size != 2) {
            throw IllegalStateException("With id option was specified but no id was found")
        }
        return Pair(idTransform(idAndPassage[0]), idAndPassage[1])
    }
}
