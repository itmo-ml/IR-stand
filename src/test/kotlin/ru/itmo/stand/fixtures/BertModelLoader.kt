package ru.itmo.stand.fixtures

import ru.itmo.stand.service.bert.BertModelLoader
import ru.itmo.stand.service.bert.DefaultBertTranslator

fun bertModelLoader(): BertModelLoader = BertModelLoader(DefaultBertTranslator(), standProperties())
