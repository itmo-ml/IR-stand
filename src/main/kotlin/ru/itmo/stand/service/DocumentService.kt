package ru.itmo.stand.service

import ru.itmo.stand.config.Method

interface DocumentService {

    val method: Method

    fun find(id: String): String?

    fun search(query: String): List<String>

    fun save(content: String, withId: Boolean): String

    fun saveInBatch(contents: List<String>, withId: Boolean): List<String>

    fun getFootprint(): String?

    fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")

    fun extractId(content: String, withId: Boolean): Pair<Long?, String> {
        return if (withId) {
            val idAndPassage = content.split("\t");
            if (idAndPassage.size != 2) {
                throw IllegalStateException("With id option was specified but no id was found")
            }
            Pair(idAndPassage[0].toLong(), idAndPassage[1]);
        } else {
            Pair(null, content);
        }
    }
}
