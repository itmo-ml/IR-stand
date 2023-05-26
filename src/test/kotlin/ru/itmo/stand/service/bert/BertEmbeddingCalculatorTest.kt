package ru.itmo.stand.service.bert

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.standProperties

class BertEmbeddingCalculatorTest {

    private val embeddingCalculator = BertEmbeddingCalculator(
        BertModelLoader(DefaultBertTranslator(), standProperties()),
        standProperties(),
    )
    private val precision = 0.00_001f

    @Test
    fun `should return the same result as for Python`() {
        val content = "test 1"

        val embedding = embeddingCalculator.calculate(TranslatorInput(0, content))

        assertThat(embedding)
            .usingComparatorWithPrecision(precision)
            .startsWith(0.63_738f, -0.14_589f, -0.13_540f, 0.10_968f, 0.69_273f)
    }

    @Test
    fun `should return same result for batch mode`() {
        val content1 = "test 1"
        val content2 = "test 2 3"
        val content3 = "test 3 4 5"
        val embedding1 = embeddingCalculator.calculate(TranslatorInput(0, content1))
        val embedding2 = embeddingCalculator.calculate(TranslatorInput(0, content2))
        val embedding3 = embeddingCalculator.calculate(TranslatorInput(0, content3))

        val embeddingsBatch = embeddingCalculator.calculate(
            arrayOf(
                TranslatorInput(0, content1),
                TranslatorInput(0, content2),
                TranslatorInput(0, content3),
            ),
        )

        assertThat(embeddingsBatch[0]).usingComparatorWithPrecision(precision).containsExactly(*embedding1)
        assertThat(embeddingsBatch[1]).usingComparatorWithPrecision(precision).containsExactly(*embedding2)
        assertThat(embeddingsBatch[2]).usingComparatorWithPrecision(precision).containsExactly(*embedding3)
    }
}
