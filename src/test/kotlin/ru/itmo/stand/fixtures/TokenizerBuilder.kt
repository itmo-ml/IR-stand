package ru.itmo.stand.fixtures

import ru.itmo.stand.service.preprocessing.Tokenizer

fun tokenizer(): Tokenizer = Tokenizer(stanfordCoreNLP())
