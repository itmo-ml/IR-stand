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

import ai.djl.huggingface.tokenizers.Encoding
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.translate.ArgumentsUtil
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import ru.itmo.stand.service.bert.CustomTranslatorInput
import java.io.IOException

/** The translator for Huggingface text embedding model.  */
class CustomEmbeddingTranslator internal constructor(
    private val tokenizer: HuggingFaceTokenizer,
    private val batchifier: Batchifier,
    private val normalize: Boolean,
) : Translator<CustomTranslatorInput?, FloatArray?> {
    /** {@inheritDoc}  */
    override fun getBatchifier(): Batchifier {
        return batchifier
    }

    /** {@inheritDoc}  */
    override fun processInput(ctx: TranslatorContext, input: CustomTranslatorInput?): NDList {
        val encoding = tokenizer.encode(input?.window)
        ctx.setAttachment("encoding", encoding)
        ctx.setAttachment("index", input?.middleTokenIndex)
        ctx.setAttachment("pooling", input?.pooling)
        return encoding.toNDList(ctx.ndManager, false)
    }

    /** {@inheritDoc}  */
    override fun processOutput(ctx: TranslatorContext, list: NDList): FloatArray {
        val encoding = ctx.getAttachment("encoding") as Encoding
        val index = ctx.getAttachment("index") as Long
        val pooling = ctx.getAttachment("pooling") as String
        val manager = ctx.ndManager
        var embeddings = processEmbedding(
            manager,
            list,
            encoding,
            pooling,
            index,
        )
        if (normalize) {
            embeddings = embeddings.normalize(2.0, 0)
        }
        return embeddings.toFloatArray()
    }

    /** {@inheritDoc}  */
    override fun toBatchTranslator(batchifier: Batchifier): CustomEmbeddingBatchTranslator {
        tokenizer.enableBatch()
        return CustomEmbeddingBatchTranslator(tokenizer, batchifier, normalize)
    }

    /** The builder for token classification translator.  */
    class Builder internal constructor(private val tokenizer: HuggingFaceTokenizer) {
        private var batchifier = Batchifier.STACK
        private var normalize = true

        /**
         * Sets the [Batchifier] for the [Translator].
         *
         * @param batchifier true to include token types
         * @return this builder
         */
        fun optBatchifier(batchifier: Batchifier): Builder {
            this.batchifier = batchifier
            return this
        }

        /**
         * Sets the `normalize` for the [Translator].
         *
         * @param normalize true to normalize the embeddings
         * @return this builder
         */
        fun optNormalize(normalize: Boolean): Builder {
            this.normalize = normalize
            return this
        }

        /**
         * Configures the builder with the model arguments.
         *
         * @param arguments the model arguments
         */
        fun configure(arguments: Map<String?, *>?) {
            val batchifierStr = ArgumentsUtil.stringValue(arguments, "batchifier", "stack")
            optBatchifier(Batchifier.fromString(batchifierStr))
            optNormalize(ArgumentsUtil.booleanValue(arguments, "normalize", true))
        }

        /**
         * Builds the translator.
         *
         * @return the new translator
         * @throws IOException if I/O error occurs
         */
        @Throws(IOException::class)
        fun build(): CustomEmbeddingTranslator {
            return CustomEmbeddingTranslator(tokenizer, batchifier, normalize)
        }
    }

    companion object {
        private val AXIS = intArrayOf(0)
        fun processEmbedding(
            manager: NDManager,
            list: NDList,
            encoding: Encoding,
            pooling: String,
            tokenIndex: Long,
        ): NDArray {
            val embedding = list["last_hidden_state"]
            val attentionMask = encoding.attentionMask
            val inputAttentionMask = manager.create(attentionMask).toType(DataType.FLOAT32, true)
            when (pooling) {
                "mean" -> return meanPool(embedding, inputAttentionMask, false)
                "mean_sqrt_len" -> return meanPool(embedding, inputAttentionMask, true)
                "max" -> return maxPool(embedding, inputAttentionMask)
                "weightedmean" -> return weightedMeanPool(embedding, inputAttentionMask)
                "cls" -> return embedding[0]
                // cls token is first
                "token" -> return embedding[tokenIndex + 1]
                else -> throw AssertionError("Unexpected pooling mode: $pooling")
            }
        }

        private fun meanPool(embeddings: NDArray, attentionMask: NDArray, sqrt: Boolean): NDArray {
            var attentionMask = attentionMask
            val shape = embeddings.shape.shape
            attentionMask = attentionMask.expandDims(-1).broadcast(*shape)
            val inputAttentionMaskSum = attentionMask.sum(AXIS)
            val clamp = inputAttentionMaskSum.clip(1e-9, 1e12)
            val prod = embeddings.mul(attentionMask)
            val sum = prod.sum(AXIS)
            return if (sqrt) {
                sum.div(clamp.sqrt())
            } else {
                sum.div(clamp)
            }
        }

        private fun maxPool(embeddings: NDArray, inputAttentionMask: NDArray): NDArray {
            var embeddings = embeddings
            var inputAttentionMask = inputAttentionMask
            val shape = embeddings.shape.shape
            inputAttentionMask = inputAttentionMask.expandDims(-1).broadcast(*shape)
            inputAttentionMask = inputAttentionMask.eq(0)
            embeddings = embeddings.duplicate()
            embeddings[inputAttentionMask] = -1e9 // Set padding tokens to large negative value
            return embeddings.max(AXIS, true)
        }

        private fun weightedMeanPool(embeddings: NDArray, attentionMask: NDArray): NDArray {
            var attentionMask = attentionMask
            val shape = embeddings.shape.shape
            var weight = embeddings.manager.arange(1f, (shape[0] + 1).toFloat())
            weight = weight.expandDims(-1).broadcast(*shape)
            attentionMask = attentionMask.expandDims(-1).broadcast(*shape).mul(weight)
            val maskSum = attentionMask.sum(AXIS)
            val embeddingSum = embeddings.mul(attentionMask).sum(AXIS)
            return embeddingSum.div(maskSum)
        }

        /**
         * Creates a builder to build a `TextEmbeddingTranslator`.
         *
         * @param tokenizer the tokenizer
         * @return a new builder
         */
        fun builder(tokenizer: HuggingFaceTokenizer): Builder {
            return Builder(tokenizer)
        }

        /**
         * Creates a builder to build a `TextEmbeddingTranslator`.
         *
         * @param tokenizer the tokenizer
         * @param arguments the models' arguments
         * @return a new builder
         */
        fun builder(tokenizer: HuggingFaceTokenizer, arguments: Map<String?, *>?): Builder {
            val builder = builder(tokenizer)
            builder.configure(arguments)
            return builder
        }
    }
}
