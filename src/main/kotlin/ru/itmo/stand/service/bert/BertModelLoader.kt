package ru.itmo.stand.service.bert

import ai.djl.Application
import ai.djl.inference.Predictor
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties

@Service
class BertModelLoader(
    private val standProperties: StandProperties,
    private val bertTranslator: BertTranslator,
) {

    private val model: ZooModel<String, FloatArray> by lazy {
        val basePath = standProperties.app.basePath
        Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(String::class.java, FloatArray::class.java)
            .optModelUrls("$basePath/models/distilbert.pt")
            .optTranslator(bertTranslator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
    }

    fun load(): ZooModel<String, FloatArray> = model
    fun defaultPredictor(): Predictor<String, FloatArray> = model.newPredictor()
}
