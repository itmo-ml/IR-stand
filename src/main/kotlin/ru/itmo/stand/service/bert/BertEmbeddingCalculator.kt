package ru.itmo.stand.service.bert

import org.springframework.stereotype.Service

@Service
class BertEmbeddingCalculator(
    private val bertModelLoader: BertModelLoader,
) {

    private val predictor by lazy { bertModelLoader.defaultPredictor() }

    fun calculate(content: String): FloatArray = predictor.predict(content)

    fun calculate(contents: List<String>): List<FloatArray> =
        contents.map { predictor.predict(it) } // TODO: replace on batchPredict
}
