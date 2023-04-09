package ru.itmo.stand.service.bert

import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties

@Service
class BertEmbeddingCalculator(
    private val bertModelLoader: BertModelLoader,
    private val standProperties: StandProperties,
) {

    // TODO: configure to return vector for middle token
    private val predictor by lazy {
        bertModelLoader.loadModel(standProperties.app.neighboursAlgorithm.bertModelType).newPredictor()
    }

    fun calculate(content: String): FloatArray = predictor.predict(arrayOf(content)).first()

    fun calculate(contents: Array<String>): Array<FloatArray> = predictor.predict(contents)

    fun calculate(contents: Collection<String>, batchSize: Int): Array<FloatArray> = contents.chunked(batchSize)
        .flatMap { chunk -> predictor.predict(chunk.toTypedArray()).asIterable() }
        .toTypedArray()
}
