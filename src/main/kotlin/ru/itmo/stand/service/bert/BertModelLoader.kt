package ru.itmo.stand.service.bert

import EmbeddingTranslatorFactory
import ai.djl.Application
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import ai.djl.translate.Translator
import org.springframework.stereotype.Service
import ru.itmo.stand.config.BertModelType
import ru.itmo.stand.config.StandProperties
import java.nio.file.Paths

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
            .setTypes(Array<TranslatorInput>::class.java, Array<FloatArray>::class.java)
            .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/msmarco-distilbert-dot-v5")
            .optEngine("PyTorch")
            .optArgument("padding", "true")
            .optArgument("normalize", "false")
            .optTranslatorFactory(TextEmbeddingTranslatorFactory())
            .build()
            .loadModel()
    }

    private val tinyModel by lazy {
        val basePath = standProperties.app.basePath

        Criteria.builder()
            .setTypes(Array<TranslatorInput>::class.java, Array<FloatArray>::class.java)
            .optModelName("prajjwal1/bert-tiny")
            .optModelPath(Paths.get("$basePath/models/bert-tiny"))
            .optEngine("PyTorch")
            .optArgument("padding", "true")
            .optArgument("normalize", "false")
            .optArgument("pooling", "token")
            .optArgument("maxLength", "20")
            .optTranslatorFactory(EmbeddingTranslatorFactory())
            .build()
            .loadModel()
    }

    private val models = mapOf(
        BertModelType.BASE to lazy { defaultModel },
        BertModelType.TINY to lazy { tinyModel },
    )

    fun loadModel(type: BertModelType): ZooModel<Array<TranslatorInput>, Array<FloatArray>> {
        if (!models.containsKey(type)) {
            throw IllegalArgumentException(type.name)
        }
        return models[type]!!.value
    }

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
