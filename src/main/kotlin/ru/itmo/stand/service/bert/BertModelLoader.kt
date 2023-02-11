package ru.itmo.stand.service.bert

import ai.djl.Application
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory
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

    private val deprecatedModel: ZooModel<String, FloatArray> by lazy {
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

    @Deprecated("Loading from a file is not preferred. Also this method does not support padding batching")
    fun deprecatedModel(): ZooModel<String, FloatArray> = deprecatedModel

    private val defaultModel by lazy {
        Criteria.builder()
            .setTypes(Array<String>::class.java, Array<DoubleArray>::class.java)
            .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/msmarco-distilbert-dot-v5")
            .optEngine("PyTorch")
            .optArgument("padding", "true")
            .optArgument("normalize", "false")
            .optTranslatorFactory(TextEmbeddingTranslatorFactory())
            .build()
            .loadModel()
    }

    fun defaultModel(): ZooModel<Array<String>, Array<DoubleArray>> = defaultModel

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
