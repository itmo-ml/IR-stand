package ru.itmo.stand.service.bert

import org.springframework.stereotype.Service

@Service
class BertEmbeddingCalculator(
    private val bertModelLoader: BertModelLoader,
    private val bertTranslator: BertTranslator,
) {

    private val predictor by lazy { bertModelLoader.loadModel(bertTranslator).newPredictor() }

    fun calculate(content: List<String>): FloatArray = predictor.predict(content)

    fun calculate(contents: List<List<String>>): List<FloatArray> =
        contents.map { predictor.predict(it) } // TODO: replace on batchPredict
}
