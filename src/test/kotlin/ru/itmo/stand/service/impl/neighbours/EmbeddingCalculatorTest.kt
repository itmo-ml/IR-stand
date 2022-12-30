package ru.itmo.stand.service.impl.neighbours

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.standProperties
import ru.itmo.stand.service.bert.BertModelLoader
import ru.itmo.stand.service.bert.BertTranslator
import ru.itmo.stand.util.Window

class EmbeddingCalculatorTest {

    private val embeddingCalculator = EmbeddingCalculator(BertModelLoader(standProperties(), BertTranslator()))

    @Test
    fun `should return same result for batch mode`() {
        val window1 = Window(content = listOf("test", "1"))
        val window2 = Window(content = listOf("test", "2"))
        val window3 = Window(content = listOf("test", "3"))
        val vector1 = embeddingCalculator.calculate(window1)
        val vector2 = embeddingCalculator.calculate(window2)
        val vector3 = embeddingCalculator.calculate(window3)

        val embeddingsBatch = embeddingCalculator.calculate(listOf(window1, window2, window3))

        assertThat(embeddingsBatch)
            .usingRecursiveComparison()
            .isEqualTo(listOf(vector1, vector2, vector3))
    }
}
