package ru.itmo.stand.util

const val SEP_TOKEN = "[SEP]"
const val CLS_TOKEN = "[CLS]"
const val TOKEN_SEPARATOR = "tokenseparator"
const val UNKNOWN_TOKEN = "[UNK]"

fun MutableList<String>.wrapToClsAndSep() {
    this.add(0, CLS_TOKEN)
    this.add(SEP_TOKEN)
}
