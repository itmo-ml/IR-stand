package ru.itmo.stand.service.impl.neighbours

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import ru.itmo.stand.util.Window

class WindowsPipelineExecutorTest {

    private val preprocessingPipelineExecutor = preprocessingPipelineExecutor()

    @Test
    fun `should convert to lemmas, remove stop words and convert to windows`() {
        val result = preprocessingPipelineExecutor.execute("The quick brown fox .jumps ,over ^the :lazy #dog!!!")

        assertEquals(
            listOf(
                Window("quick", 0, listOf("quick", "brown", "fox")),
                Window("brown", 1, listOf("quick", "brown", "fox", "jump")),
                Window("fox", 2, listOf("quick", "brown", "fox", "jump", "lazy")),
                Window("jump", 2, listOf("brown", "fox", "jump", "lazy", "dog")),
                Window("lazy", 2, listOf("fox", "jump", "lazy", "dog")),
                Window("dog", 2, listOf("jump", "lazy", "dog")),
            ),
            result,
        )
    }
}
