package ru.itmo.stand.service.preprocessing

import org.springframework.stereotype.Service

@Service
class TextCleaner : Preprocessor<String, String> {

    private val regex = Regex("[^A-Za-z0-9 ]")

    override fun preprocess(input: String): String = input.replace(regex, "")
}