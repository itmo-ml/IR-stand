package ru.itmo.stand.service.bert

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.standProperties

class BertEmbeddingCalculatorTest {

    private val embeddingCalculator = BertEmbeddingCalculator(
        BertModelLoader(DefaultBertTranslator(), standProperties()),
    )

    @Test
    fun `should return the same result as for Python`() {
        val content = "test 1"

        val embedding = embeddingCalculator.calculate(content)

        assertThat(embedding)
            .usingComparatorWithPrecision(0.000_001f)
            .startsWith(0.216_166f, -0.093_901f, -0.137_327f, 0.097_924f, 0.406_257f)
    }

    @Test
    fun `should return same result for batch mode`() {
        val content1 = "test 1"
        val content2 = "test 2 3"
        val content3 = "test 3 4 5"
        val embedding1 = embeddingCalculator.calculate(content1)
        val embedding2 = embeddingCalculator.calculate(content2)
        val embedding3 = embeddingCalculator.calculate(content3)

        val embeddingsBatch = embeddingCalculator.calculate(arrayOf(content1, content2, content3))

        assertThat(embeddingsBatch).isEqualTo(arrayOf(embedding1, embedding2, embedding3))
    }
}
