package ru.itmo.stand.service.preprocessing

import org.springframework.stereotype.Service
import ru.itmo.stand.util.Window
import ru.itmo.stand.util.createWindows

@Service
class ContextSplitter : Preprocessor<ContextSplitter.Input, List<Window>> {

    override fun preprocess(input: Input): List<Window> {
        return input.content.createWindows(input.windowSize)
    }

    data class Input(
        val content: List<String>,
        val windowSize: Int,
    )
}
