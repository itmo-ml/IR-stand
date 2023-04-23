package ru.itmo.stand.service.bert

data class CustomTranslatorInput(
    val middleTokenIndex: Long,
    val window: String,
    val pooling: String = "token",
)
