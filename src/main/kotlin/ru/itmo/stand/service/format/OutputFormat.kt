package ru.itmo.stand.service.format

fun formatMrr(queryId: Int, docId: String, rank: Int): String = buildString {
    append(queryId)
    append("\t\t")
    append(docId)
    append("\t")
    append(rank)
}
