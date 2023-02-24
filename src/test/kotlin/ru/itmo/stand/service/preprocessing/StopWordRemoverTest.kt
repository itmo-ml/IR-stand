package ru.itmo.stand.service.preprocessing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StopWordRemoverTest {

    private val stopWordRemover = StopWordRemover()

    @Test
    fun `should remove stop words`() {
        val tokens = "The quick brown fox jumps over the lazy dog".lowercase().split(" ")
        val target = listOf("quick", "brown", "fox", "jumps", "lazy", "dog")

        val result = stopWordRemover.preprocess(tokens)

        assertEquals(target, result)
    }
}
