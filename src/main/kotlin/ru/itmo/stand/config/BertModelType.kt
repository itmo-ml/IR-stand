package ru.itmo.stand.config

enum class BertModelType(val dimensions: Int) {
    TINY(128),
    BASE(768),
}
