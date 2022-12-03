package ru.itmo.stand.util

private fun <T> extractId(content: String, idTransform: (String) -> T): Pair<T, String> {
    val idAndPassage = content.split("\t")
    if (idAndPassage.size != 2) {
        throw IllegalStateException("With id option was specified but no id was found")
    }
    return Pair(idTransform(idAndPassage[0]), idAndPassage[1])
}

fun extractId(content: String): Pair<String, String> = extractId(content) { it }

fun extractId(content: String, withId: Boolean): Pair<Long?, String> = if (withId)
    extractId(content) { it.toLong() }
else Pair(null, content)

fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")
