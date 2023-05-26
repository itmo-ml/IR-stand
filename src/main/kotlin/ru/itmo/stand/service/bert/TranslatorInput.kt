package ru.itmo.stand.service.bert

data class TranslatorInput(
    val wordIndex: Int,
    val content: String,
) {
    companion object {
        const val CLS = -1
        fun withClsWordIndex(content: String) = TranslatorInput(CLS, content)
    }
}
