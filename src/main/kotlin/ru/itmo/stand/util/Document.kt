package ru.itmo.stand.util

import ru.itmo.stand.service.model.Document

fun extractId(content: String): Document {
    val idAndPassage = content.split("\t")
    if (idAndPassage.size != 2) {
        throw IllegalArgumentException("Incorrect content format. Expected format: [ID][\\t][content]")
    }
    return Document(idAndPassage[0], idAndPassage[1])
}

fun throwDocIdNotFoundEx(): Nothing = throw IllegalStateException("Document id must not be null.")
