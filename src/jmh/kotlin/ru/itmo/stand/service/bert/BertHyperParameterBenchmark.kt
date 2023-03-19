package ru.itmo.stand.service.bert

import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory
import ai.djl.repository.zoo.Criteria
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Benchmark
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import java.nio.file.Paths

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class BertHyperParameterBenchmark {

    private val contents = (
        "Definition of Central nervous system (CNS) Central nervous system (CNS): " +
            "The central nervous system is that part of the nervous system that consists " +
            "of the brain and spinal cord. The central nervous system (CNS) is one of the " +
            "two major divisions of the nervous system. " +
            "The other is the peripheral nervous system (PNS) which is outside the brain and spinal cord. " +
            "The peripheral nervous system (PNS) connects the central nervous system (CNS) " +
            "to sensory organs (such as the eye and ear), other organs of the body, muscles, " +
            "blood vessels and glands."
        )
        .let { preprocessingPipelineExecutor().execute(it) }
        .map { it.content.joinToString(" ") }

    private val tinyModel =
        Criteria.builder()
            .setTypes(Array<String>::class.java, Array<FloatArray>::class.java)
            .optModelName("prajjwal1/bert-tiny")
            .optModelPath(Paths.get("./models/bert-tiny"))
            .optEngine("PyTorch")
            .optArgument("padding", "true")
            .optArgument("normalize", "false")
            .optArgument("pooling", "cls")
            .optTranslatorFactory(TextEmbeddingTranslatorFactory())
            .build()
            .loadModel()
            .newPredictor()

    private val testContents = generateWindows()

    @Benchmark
    fun singleThreadBenchmark_100(): Array<FloatArray> {
        return singleThreadBenchmark(100)
    }

    @Benchmark
    fun singleThreadBenchmark_200(): Array<FloatArray> {
        return singleThreadBenchmark(200)
    }

    @Benchmark
    fun singleThreadBenchmark_500(): Array<FloatArray> {
        return singleThreadBenchmark(500)
    }

    @Benchmark
    fun singleThreadBenchmark_1000(): Array<FloatArray> {
        return singleThreadBenchmark(1000)
    }

    @Benchmark
    fun singleThreadBenchmark_2000(): Array<FloatArray> {
        return singleThreadBenchmark(2000)
    }

    @Benchmark
    fun singleThreadBenchmark_5000(): Array<FloatArray> {
        return singleThreadBenchmark(5000)
    }

    @Benchmark
    fun singleThreadBenchmark_10_000(): Array<FloatArray> {
        return singleThreadBenchmark(10_000)
    }

    @Benchmark
    fun singleThreadBenchmark_15_000(): Array<FloatArray> {
        return singleThreadBenchmark(15_000)
    }

    @Benchmark
    fun singleThreadBenchmark_20_000(): Array<FloatArray> {
        return singleThreadBenchmark(20_000)
    }

    @Benchmark
    fun singleThreadBenchmark_25_000(): Array<FloatArray> {
        return singleThreadBenchmark(25_000)
    }

    @Benchmark
    fun singleThreadBenchmark_50_000(): Array<FloatArray> {
        return singleThreadBenchmark(50_000)
    }

    private fun singleThreadBenchmark(batchSize: Int): Array<FloatArray> {
        return testContents.chunked(batchSize)
            .flatMap {
                tinyModel.predict(it.toTypedArray()).asIterable()
            }.toTypedArray()
    }

    private fun generateWindows(count: Int = 50_000): List<String> {
        val result = mutableListOf<String>()
        for (i in 0 until count) {
            result.add(i, contents.random())
        }
        return result
    }
}
