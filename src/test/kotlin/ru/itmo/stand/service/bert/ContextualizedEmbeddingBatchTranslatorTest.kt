package ru.itmo.stand.service.bert

import ai.djl.ndarray.NDList
import ai.djl.ndarray.types.Shape
import ai.djl.nn.Block
import ai.djl.nn.LambdaBlock
import ai.djl.repository.zoo.Criteria
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files

class ContextualizedEmbeddingBatchTranslatorTest {

    private val directory = Files.createTempDirectory("contextualized-embedding-batch-translator-test")
    private val batchBlock: Block = LambdaBlock { ndList: NDList ->
        val manager = ndList.manager
        val arr = manager.ones(Shape(2, 7, 128))
            .cumSum(1)
            .cumSum(1)
        arr.name = "last_hidden_state"
        NDList(arr)
    }

    @Nested
    inner class TokenPooling {

        private val predictor = Criteria.builder()
            .setTypes(Array<TranslatorInput>::class.java, Array<FloatArray>::class.java)
            .optModelPath(directory)
            .optBlock(batchBlock)
            .optEngine("PyTorch")
            .optArgument("tokenizer", "bert-base-uncased")
            .optArgument("normalize", false)
            .optArgument("pooling", "token")
            .optOption("hasParameter", "false")
            .optTranslatorFactory(ContextualizedEmbeddingTranslatorFactory())
            .build()
            .loadModel()
            .newPredictor()

        @Test
        fun `should return average embeddings for middle words consisting of one and two tokens`() {
            val firstMiddleWord = "dancing"
            val firstSentence = "They went $firstMiddleWord every weekend"
            val secondMiddleWord = "snowboarding"
            val secondSentence = "They went $secondMiddleWord yesterday"
            val input = arrayOf(
                TranslatorInput(firstSentence.split(" ").indexOf(firstMiddleWord), firstSentence),
                TranslatorInput(secondSentence.split(" ").indexOf(secondMiddleWord), secondSentence),
            )

            val result = predictor.predict(input)

            assertThat(result[0]).isEqualTo(FloatArray(128) { 10f })
            assertThat(result[1]).isEqualTo(FloatArray(128) { 12.5f })
        }

        @Test
        fun `should do padding on different window sizes`() {
            val firstMiddleWord = "sentence"
            val firstSentence = "Short $firstMiddleWord"
            val secondMiddleWord = "snowboarding"
            val secondSentence = "They went $secondMiddleWord yesterday"
            val input = arrayOf(
                TranslatorInput(firstSentence.split(" ").indexOf(firstMiddleWord), firstSentence),
                TranslatorInput(secondSentence.split(" ").indexOf(secondMiddleWord), secondSentence),
            )

            val result = predictor.predict(input)

            assertThat(result[0]).isEqualTo(FloatArray(128) { 6f })
            assertThat(result[1]).isEqualTo(FloatArray(128) { 12.5f })
        }
    }

    @Nested
    inner class ClsPolling {

        private val predictor = Criteria.builder()
            .setTypes(Array<TranslatorInput>::class.java, Array<FloatArray>::class.java)
            .optModelPath(directory)
            .optBlock(batchBlock)
            .optEngine("PyTorch")
            .optArgument("tokenizer", "bert-base-uncased")
            .optArgument("normalize", false)
            .optArgument("pooling", "cls")
            .optOption("hasParameter", "false")
            .optTranslatorFactory(ContextualizedEmbeddingTranslatorFactory())
            .build()
            .loadModel()
            .newPredictor()

        @Test
        fun `should return average embeddings for middle words consisting of one and two tokens`() {
            val firstMiddleWord = "dancing"
            val firstSentence = "They went $firstMiddleWord every weekend"
            val secondMiddleWord = "snowboarding"
            val secondSentence = "They went $secondMiddleWord yesterday"
            val input = arrayOf(
                TranslatorInput(firstSentence.split(" ").indexOf(firstMiddleWord), firstSentence),
                TranslatorInput(secondSentence.split(" ").indexOf(secondMiddleWord), secondSentence),
            )

            val result = predictor.predict(input)

            assertThat(result[0]).isEqualTo(FloatArray(128) { 1f })
            assertThat(result[1]).isEqualTo(FloatArray(128) { 1f })
        }

        @Test
        fun `should do padding on different window sizes`() {
            val firstMiddleWord = "sentence"
            val firstSentence = "Short $firstMiddleWord"
            val secondMiddleWord = "snowboarding"
            val secondSentence = "They went $secondMiddleWord yesterday"
            val input = arrayOf(
                TranslatorInput(firstSentence.split(" ").indexOf(firstMiddleWord), firstSentence),
                TranslatorInput(secondSentence.split(" ").indexOf(secondMiddleWord), secondSentence),
            )

            val result = predictor.predict(input)

            assertThat(result[0]).isEqualTo(FloatArray(128) { 1f })
            assertThat(result[1]).isEqualTo(FloatArray(128) { 1f })
        }
    }
}
