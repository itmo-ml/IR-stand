package ru.itmo.stand.service.impl.neighbours

import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.preprocessing.ContextSplitter
import ru.itmo.stand.service.preprocessing.StopWordRemover
import ru.itmo.stand.service.preprocessing.TextCleaner
import ru.itmo.stand.service.preprocessing.Tokenizer
import ru.itmo.stand.util.Window

@Service
class WindowsPipelineExecutor(
    private val standProperties: StandProperties,
    private val contextSplitter: ContextSplitter,
    private val stopWordRemover: StopWordRemover,
    private val textCleaner: TextCleaner,
    private val tokenizer: Tokenizer,
) {

    fun execute(content: String): List<Window> {
        val cleanedContent = textCleaner.preprocess(content)
        val tokens = tokenizer.preprocess(cleanedContent)
        val tokensWithoutStopWords = stopWordRemover.preprocess(tokens)
        val windowSize = standProperties.app.neighboursAlgorithm.tokenBatchSize
        return contextSplitter.preprocess(ContextSplitter.Input(tokensWithoutStopWords, windowSize))
    }
}
