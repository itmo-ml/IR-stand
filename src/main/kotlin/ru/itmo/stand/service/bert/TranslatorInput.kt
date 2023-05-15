package ru.itmo.stand.service.bert

data class TranslatorInput(
    val middleWordIndex: Int,
    val content: String,
)
