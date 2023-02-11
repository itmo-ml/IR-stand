package ru.itmo.stand.service.bert

import org.springframework.stereotype.Service

@Service
class BertEmbeddingCalculator(private val bertModelLoader: BertModelLoader) {

    private val predictor by lazy { bertModelLoader.defaultModel().newPredictor() }

    fun calculate(content: String): DoubleArray = predictor.predict(arrayOf(content)).first()

    fun calculate(contents: Array<String>): Array<DoubleArray> = predictor.predict(contents)
}
