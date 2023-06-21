package ru.itmo.stand.service.impl.neighbours

import org.springframework.stereotype.Service
import ru.itmo.stand.service.preprocessing.StopWordRemover
import ru.itmo.stand.service.preprocessing.TextCleaner
import ru.itmo.stand.service.preprocessing.Tokenizer

@Service
class TokensPipelineExecutor(
    private val stopWordRemover: StopWordRemover,
    private val textCleaner: TextCleaner,
    private val tokenizer: Tokenizer,
) {

    fun execute(content: String): List<String> {
        val cleanedContent = textCleaner.preprocess(content)
        val tokens = tokenizer.preprocess(cleanedContent)
        return stopWordRemover.preprocess(tokens)
    }
}
