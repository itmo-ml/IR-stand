package ru.itmo.stand.service.impl.neighbours

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.itmo.stand.config.NlpConfig.Companion.ANNOTATORS
import ru.itmo.stand.fixtures.standProperties
import ru.itmo.stand.service.preprocessing.ContextSplitter
import ru.itmo.stand.service.preprocessing.StopWordRemover
import ru.itmo.stand.service.preprocessing.Tokenizer
import ru.itmo.stand.util.Window
import java.util.Properties

class PreprocessingPipelineExecutorTest {

    private val preprocessingPipelineExecutor = PreprocessingPipelineExecutor(
        standProperties(),
        ContextSplitter(),
        StopWordRemover(),
        Tokenizer(StanfordCoreNLP(Properties().apply { setProperty("annotators", ANNOTATORS) }))
    )

    @Test
    fun `should convert to lemmas, remove stop words and convert to windows`() {
        val result = preprocessingPipelineExecutor.execute("The quick brown fox jumps over the lazy dog")

        assertEquals(
            listOf(
                Window("quick", listOf("quick", "brown", "fox")),
                Window("brown", listOf("quick", "brown", "fox", "jump")),
                Window("fox", listOf("quick", "brown", "fox", "jump", "lazy")),
                Window("jump", listOf("brown", "fox", "jump", "lazy", "dog")),
                Window("lazy", listOf("fox", "jump", "lazy", "dog")),
                Window("dog", listOf("jump", "lazy", "dog")),
            ),
            result,
        )
    }
}
