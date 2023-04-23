package ru.itmo.stand.service.bert

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.standProperties

class BertEmbeddingCalculatorTest {

    private val embeddingCalculator = BertEmbeddingCalculator(
        BertModelLoader(DefaultBertTranslator(), standProperties()),
        standProperties(),
    )
    private val precision = 0.000_001f

    @Test
    fun `should return the same result as for Python`() {
        val content = "test 1"

        val embedding = embeddingCalculator.calculate(CustomTranslatorInput(-1, content))

        assertThat(embedding)
            .usingComparatorWithPrecision(precision)
            .startsWith(0.216_166f, -0.093_901f, -0.137_327f, 0.097_924f, 0.406_257f)
    }

    @Test
    fun `should return same result for batch mode`() {
        val content1 = "test 1"
        val content2 = "test 2 3"
        val content3 = "test 3 4 5"
        val embedding1 = embeddingCalculator.calculate(CustomTranslatorInput(-1, content1))
        val embedding2 = embeddingCalculator.calculate(CustomTranslatorInput(-1, content2))
        val embedding3 = embeddingCalculator.calculate(CustomTranslatorInput(-1, content3))

        val embeddingsBatch = embeddingCalculator.calculate(
            arrayOf(
                CustomTranslatorInput(-1, content1),
                CustomTranslatorInput(-1, content2),
                CustomTranslatorInput(-1, content3),
            ),
        )

        assertThat(embeddingsBatch[0]).usingComparatorWithPrecision(precision).containsExactly(*embedding1)
        assertThat(embeddingsBatch[1]).usingComparatorWithPrecision(precision).containsExactly(*embedding2)
        assertThat(embeddingsBatch[2]).usingComparatorWithPrecision(precision).containsExactly(*embedding3)
    }
}
