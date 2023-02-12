package ru.itmo.stand.service.preprocessing

interface Preprocessor<T, R> {
    fun preprocess(input: T): R
}
