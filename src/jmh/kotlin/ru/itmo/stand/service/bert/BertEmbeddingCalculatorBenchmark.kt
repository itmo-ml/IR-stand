package ru.itmo.stand.service.bert

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import ru.itmo.stand.fixtures.standProperties

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
open class BertEmbeddingCalculatorBenchmark {

    private val bertModelLoader = BertModelLoader(DefaultBertTranslator(), standProperties())
    private val predictor = bertModelLoader.loadModel(BertTranslator()).newPredictor()
    private val contents = ("Definition of Central nervous system (CNS) Central nervous system (CNS): " +
        "The central nervous system is that part of the nervous system that consists " +
        "of the brain and spinal cord. The central nervous system (CNS) is one of the " +
        "two major divisions of the nervous system. " +
        "The other is the peripheral nervous system (PNS) which is outside the brain and spinal cord. " +
        "The peripheral nervous system (PNS) connects the central nervous system (CNS) " +
        "to sensory organs (such as the eye and ear), other organs of the body, muscles, " +
        "blood vessels and glands.")
        .let { preprocessingPipelineExecutor().execute(it) }
        .map { it.content }

    @Benchmark
    fun calculateOneByOne(): List<FloatArray> {
        return contents.map { predictor.predict(it) }
    }

    @Benchmark
    fun calculateInStackBatch(): List<FloatArray> {
        val result = mutableListOf<FloatArray>()
        val batches = mutableListOf<List<List<String>>>()
        val currentBatch = mutableListOf<List<String>>()
        var previousSize: Int? = null
        for (content in contents) {
            when (previousSize) {
                null -> currentBatch.add(content)
                content.size -> currentBatch.add(content)
                else -> {
                    batches.add(ArrayList(currentBatch))
                    currentBatch.clear()
                    currentBatch.add(content)
                }
            }
            previousSize = content.size
        }
        if (currentBatch.isNotEmpty()) batches.add(currentBatch)
        for (batch in batches) {
            result += predictor.batchPredict(batch)
        }
        return result
    }
}
