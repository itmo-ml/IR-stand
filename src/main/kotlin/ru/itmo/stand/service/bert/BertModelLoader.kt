package ru.itmo.stand.service.bert

import ai.djl.Application
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties

@Service
class BertModelLoader(
    private val defaultBertTranslator: DefaultBertTranslator,
    val standProperties: StandProperties,
) {

    private val defaultModel: ZooModel<String, FloatArray> by lazy {
        val basePath = standProperties.app.basePath
        Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(String::class.java, FloatArray::class.java)
            .optModelUrls("$basePath/models/distilbert.pt")
            .optTranslator(defaultBertTranslator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
    }

    fun defaultModel(): ZooModel<String, FloatArray> = defaultModel

    final inline fun <reified I, reified O> loadModel(translator: Translator<I, O>): ZooModel<I, O> =
        Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(I::class.java, O::class.java)
            .optModelUrls("${standProperties.app.basePath}/models/distilbert.pt")
            .optTranslator(translator)
            .optProgress(ProgressBar())
            .build()
            .loadModel()
}
