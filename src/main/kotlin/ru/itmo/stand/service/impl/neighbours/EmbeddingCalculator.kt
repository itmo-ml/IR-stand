package ru.itmo.stand.service.impl.neighbours

import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertModelLoader
import ru.itmo.stand.util.Window

@Service
class EmbeddingCalculator(
    private val bertModelLoader: BertModelLoader,
) {

    private val predictor by lazy { bertModelLoader.defaultPredictor() }

    fun calculate(window: Window): FloatArray = predictor.predict(window.convertContentToString())

    fun calculate(windows: Collection<Window>): List<FloatArray> =
        windows.map { predictor.predict(it.convertContentToString()) } // TODO: replace on batchPredict
}
