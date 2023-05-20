package ru.itmo.stand.service.bert

import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties

@Service
class BertEmbeddingCalculator(
    private val bertModelLoader: BertModelLoader,
    private val standProperties: StandProperties,
) {

    private val predictor by lazy {
        bertModelLoader.loadModel(standProperties.app.neighboursAlgorithm.bertModelType).newPredictor()
    }

    fun calculate(input: TranslatorInput): FloatArray = predictor.predict(arrayOf(input)).first()

    fun calculate(inputs: Array<TranslatorInput>): Array<FloatArray> = predictor.predict(inputs)

    fun calculate(inputs: Collection<TranslatorInput>, batchSize: Int): Array<FloatArray> = inputs.chunked(batchSize)
        .flatMap { chunk -> predictor.predict(chunk.toTypedArray()).asIterable() }
        .toTypedArray()
}
