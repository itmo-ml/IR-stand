package ru.itmo.stand.fixtures

import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.preprocessing.ContextSplitter
import ru.itmo.stand.service.preprocessing.StopWordRemover

fun preprocessingPipelineExecutor(): PreprocessingPipelineExecutor = PreprocessingPipelineExecutor(
    standProperties(),
    ContextSplitter(),
    StopWordRemover(),
    tokenizer(),
)
