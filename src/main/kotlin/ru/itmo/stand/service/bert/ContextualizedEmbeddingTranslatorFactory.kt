package ru.itmo.stand.service.bert

import ai.djl.Model
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.translate.TranslateException
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorFactory
import java.io.IOException
import java.io.Serializable
import java.lang.reflect.Type
import ai.djl.util.Pair as DjlPair

class ContextualizedEmbeddingTranslatorFactory : TranslatorFactory, Serializable {
    override fun getSupportedTypes(): Set<DjlPair<Type, Type>> = SUPPORTED_TYPES

    override fun <I, O> newInstance(
        input: Class<I>,
        output: Class<O>,
        model: Model,
        arguments: Map<String, *>,
    ): Translator<I, O> {
        val modelPath = model.modelPath
        try {
            val tokenizer = HuggingFaceTokenizer.builder(arguments)
                .optTokenizerPath(modelPath)
                .optManager(model.ndManager)
                .build()
            val translator = ContextualizedEmbeddingTranslator.builder(tokenizer, arguments).build()
            if (input == TranslatorInput::class.java && output == FloatArray::class.java) {
                return translator as Translator<I, O>
            } else if (input == Array<TranslatorInput>::class.java && output == Array<FloatArray>::class.java) {
                return translator.toBatchTranslator() as Translator<I, O>
            }
            throw IllegalArgumentException("Unsupported input/output types.")
        } catch (e: IOException) {
            throw TranslateException("Failed to load tokenizer.", e)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        private val SUPPORTED_TYPES: Set<DjlPair<Type, Type>> = setOf(
            DjlPair(TranslatorInput::class.java, FloatArray::class.java),
            DjlPair(Array<TranslatorInput>::class.java, Array<FloatArray>::class.java),
        )
    }
}
