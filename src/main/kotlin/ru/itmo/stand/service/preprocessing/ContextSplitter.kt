package ru.itmo.stand.service.preprocessing

import org.springframework.stereotype.Service
import ru.itmo.stand.util.createContexts

@Service
class ContextSplitter : Preprocessor<ContextSplitter.Input, List<List<String>>> {

    override fun preprocess(input: Input): List<List<String>> {
        return input.content.createContexts(input.windowSize)
    }

    data class Input(
        val content: List<String>,
        val windowSize: Int,
    )
}
