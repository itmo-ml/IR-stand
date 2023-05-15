package ru.itmo.stand.service.bert

import ai.djl.ndarray.NDList
import ai.djl.ndarray.types.Shape
import ai.djl.nn.Block
import ai.djl.nn.LambdaBlock
import ai.djl.repository.zoo.Criteria
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ru.itmo.stand.util.bertTokenizer
import java.nio.file.Files

class ContextualizedEmbeddingTranslatorTest {

    private val directory = Files.createTempDirectory("contextualized-embedding-translator-test")
    private val block: Block = LambdaBlock { ndList: NDList ->
        val manager = ndList.manager
        val arr = manager.ones(Shape(1, 7, 128))
            .cumSum(1)
            .cumSum(1)
        arr.name = "last_hidden_state"
        NDList(arr)
    }

    @Nested
    inner class TokenPolling {

        private val predictor = Criteria.builder()
            .setTypes(TranslatorInput::class.java, FloatArray::class.java)
            .optModelPath(directory)
            .optBlock(block)
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
        fun `should return embedding for middle word consisting of one token`() {
            val middleWord = "dancing"
            val sentence = "They went $middleWord every weekend"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(1)
            assertThat(result).isEqualTo(FloatArray(128) { 10f })
        }

        @Test
        fun `should return average embedding for middle word consisting of two tokens`() {
            val middleWord = "snowboarding"
            val sentence = "They went $middleWord yesterday"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(2)
            assertThat(result).isEqualTo(FloatArray(128) { 12.5f })
        }

        @Test
        fun `should return average embedding for middle word consisting of three tokens`() {
            val middleWord = "pragmatic"
            val sentence = "Making $middleWord decisions"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(3)
            assertThat(result)
                .usingComparatorWithPrecision(0.001f)
                .containsExactly(*FloatArray(128) { 10.333f })
        }

        @Test
        fun `should return average embedding for middle word consisting of four tokens`() {
            val middleWord = "pharmacist"
            val sentence = "Licensed $middleWord"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(4)
            assertThat(result).isEqualTo(FloatArray(128) { 13f })
        }
    }

    @Nested
    inner class ClsPolling {

        private val predictor = Criteria.builder()
            .setTypes(TranslatorInput::class.java, FloatArray::class.java)
            .optModelPath(directory)
            .optBlock(block)
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
        fun `should return cls embedding for middle word consisting of one token`() {
            val middleWord = "dancing"
            val sentence = "They went $middleWord every weekend"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(1)
            assertThat(result).isEqualTo(FloatArray(128) { 1f })
        }

        @Test
        fun `should return cls embedding for middle word consisting of two tokens`() {
            val middleWord = "snowboarding"
            val sentence = "They went $middleWord yesterday"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(2)
            assertThat(result).isEqualTo(FloatArray(128) { 1f })
        }

        @Test
        fun `should return cls embedding for middle word consisting of three tokens`() {
            val middleWord = "pragmatic"
            val sentence = "Making $middleWord decisions"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(3)
            assertThat(result).isEqualTo(FloatArray(128) { 1f })
        }

        @Test
        fun `should return cls embedding for middle word consisting of four tokens`() {
            val middleWord = "pharmacist"
            val sentence = "Licensed $middleWord"
            val input = TranslatorInput(middleWordIndex = sentence.split(" ").indexOf(middleWord), content = sentence)

            val result = predictor.predict(input)

            assertThat(bertTokenizer.tokenize(middleWord)).hasSize(4)
            assertThat(result).isEqualTo(FloatArray(128) { 1f })
        }
    }
}
