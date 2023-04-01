package ru.itmo.stand.service.impl.neighbours

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import ru.itmo.stand.util.Window

class PreprocessingPipelineExecutorTest {

    private val preprocessingPipelineExecutor = preprocessingPipelineExecutor()

    @Test
    fun `should convert to lemmas, remove stop words and convert to windows`() {
        val result = preprocessingPipelineExecutor.execute("The quick brown fox .jumps ,over ^the :lazy #dog!!!")

        assertEquals(
            listOf(
                Window("quick", listOf("quick", "brown", "fox")),
                Window("brown", listOf("quick", "brown", "fox", "jumps")),
                Window("fox", listOf("quick", "brown", "fox", "jumps", "lazy")),
                Window("jumps", listOf("brown", "fox", "jumps", "lazy", "dog")),
                Window("lazy", listOf("fox", "jumps", "lazy", "dog")),
                Window("dog", listOf("jumps", "lazy", "dog")),
            ),
            result,
        )
    }
}
