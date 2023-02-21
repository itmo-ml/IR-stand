package ru.itmo.stand.service.bert

import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties

@Service
class BertEmbeddingCalculator(
    private val bertModelLoader: BertModelLoader,
    private val standProperties: StandProperties) {

    private val predictor by lazy {
        bertModelLoader.loadModel(standProperties.app.neighboursAlgorithm.bertModelType).newPredictor()
    }

    fun calculate(content: String): FloatArray = predictor.predict(arrayOf(content)).first()

    fun calculate(contents: Array<String>): Array<FloatArray> = predictor.predict(contents)
}
