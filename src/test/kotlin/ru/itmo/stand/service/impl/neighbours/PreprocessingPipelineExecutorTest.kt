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
                Window("quick", listOf("quick", "brown", "fox"), 0),
                Window("brown", listOf("quick", "brown", "fox", "jumps"), 1),
                Window("fox", listOf("quick", "brown", "fox", "jumps", "lazy"), 2),
                Window("jumps", listOf("brown", "fox", "jumps", "lazy", "dog"), 2),
                Window("lazy", listOf("fox", "jumps", "lazy", "dog"), 2),
                Window("dog", listOf("jumps", "lazy", "dog"), 2),
            ),
            result,
        )
    }
}
