package ru.itmo.stand.fixtures

import ru.itmo.stand.service.impl.neighbours.WindowsPipelineExecutor
import ru.itmo.stand.service.preprocessing.ContextSplitter
import ru.itmo.stand.service.preprocessing.StopWordRemover

fun preprocessingPipelineExecutor(): WindowsPipelineExecutor = WindowsPipelineExecutor(
    standProperties(),
    ContextSplitter(),
    StopWordRemover(),
    textCleaner(),
    tokenizer(),
)
