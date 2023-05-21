package ru.itmo.stand.service.bert

data class TranslatorInput(
    val middleTokenIndex: Long,
    val window: String,
    val pooling: String = "token",
)
