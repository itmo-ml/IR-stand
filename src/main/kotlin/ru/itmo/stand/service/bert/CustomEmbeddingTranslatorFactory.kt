/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

import ai.djl.Model
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.modality.Input
import ai.djl.modality.Output
import ai.djl.modality.nlp.translator.TextEmbeddingServingTranslator
import ai.djl.translate.TranslateException
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorFactory
import ai.djl.util.Pair
import java.io.IOException
import java.io.Serializable
import java.lang.reflect.Type

/** A [TranslatorFactory] that creates a [CustomEmbeddingTranslator] instance.  */
class CustomEmbeddingTranslatorFactory : TranslatorFactory, Serializable {
    /** {@inheritDoc}  */
    override fun getSupportedTypes(): Set<Pair<Type, Type>> {
        return SUPPORTED_TYPES
    }

    /** {@inheritDoc}  */
    @Throws(TranslateException::class)
    override fun <I, O> newInstance(
        input: Class<I>,
        output: Class<O>,
        model: Model,
        arguments: Map<String?, *>?,
    ): Translator<I, O> {
        val modelPath = model.modelPath
        try {
            val tokenizer = HuggingFaceTokenizer.builder(arguments)
                .optTokenizerPath(modelPath)
                .optManager(model.ndManager)
                .build()
            val translator = CustomEmbeddingTranslator.builder(tokenizer, arguments).build()
            if (input == String::class.java && output == FloatArray::class.java) {
                return translator as Translator<I, O>
            } else if (input == Array<String>::class.java && output == Array<FloatArray>::class.java) {
                return translator.toBatchTranslator() as Translator<I, O>
            } else if (input == Input::class.java && output == Output::class.java) {
                return TextEmbeddingServingTranslator(translator) as Translator<I, O>
            }
            throw IllegalArgumentException("Unsupported input/output types.")
        } catch (e: IOException) {
            throw TranslateException("Failed to load tokenizer.", e)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        private val SUPPORTED_TYPES: MutableSet<Pair<Type, Type>> = HashSet()

        init {
            SUPPORTED_TYPES.add(
                Pair(
                    String::class.java,
                    FloatArray::class.java,
                ),
            )
            SUPPORTED_TYPES.add(
                Pair(
                    Array<String>::class.java,
                    Array<FloatArray>::class.java,
                ),
            )
            SUPPORTED_TYPES.add(
                Pair(
                    Input::class.java,
                    Output::class.java,
                ),
            )
        }
    }
}
