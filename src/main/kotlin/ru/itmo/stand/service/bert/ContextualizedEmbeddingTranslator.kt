package ru.itmo.stand.service.bert

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

class ContextualizedEmbeddingTranslator internal constructor(
    private val tokenizer: HuggingFaceTokenizer,
    private val batchifier: Batchifier,
    private val normalize: Boolean,
    private val pooling: String,
) : Translator<TranslatorInput, FloatArray> {
    override fun getBatchifier(): Batchifier = batchifier

    override fun processInput(ctx: TranslatorContext, input: TranslatorInput): NDList {
        val encoding = tokenizer.encode(input.content, true)

        ctx.setAttachment("encoding", encoding)
        ctx.setAttachment("middleWordIndex", input.middleWordIndex)

        return encoding.toNDList(ctx.ndManager, false)
    }

    override fun processOutput(ctx: TranslatorContext, list: NDList): FloatArray {
        val encoding = ctx.getAttachment("encoding") as Encoding
        val middleWordIndex = ctx.getAttachment("middleWordIndex") as Int
        val manager = ctx.ndManager
        var embeddings = processEmbedding(
            manager,
            list,
            encoding,
            pooling,
            getWordRange(encoding.wordIds, middleWordIndex.toLong()),
        )
        if (normalize) {
            embeddings = embeddings.normalize(2.0, 0)
        }
        return embeddings.toFloatArray()
    }

    override fun toBatchTranslator(batchifier: Batchifier): ContextualizedEmbeddingBatchTranslator {
        tokenizer.enableBatch()
        return ContextualizedEmbeddingBatchTranslator(tokenizer, batchifier, normalize, pooling)
    }

    class Builder internal constructor(private val tokenizer: HuggingFaceTokenizer) {
        private var batchifier = Batchifier.STACK
        private var normalize = false
        private var pooling = "token"

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
         * Sets the pooling for the [Translator].
         *
         * @param pooling the pooling model
         * @return this builder
         */
        fun optPoolingMode(pooling: String): Builder {
            this.pooling = pooling
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
            optNormalize(ArgumentsUtil.booleanValue(arguments, "normalize", false))
            optPoolingMode(ArgumentsUtil.stringValue(arguments, "pooling", "token"))
        }

        fun build(): ContextualizedEmbeddingTranslator =
            ContextualizedEmbeddingTranslator(tokenizer, batchifier, normalize, pooling)
    }

    companion object {

        fun processEmbedding(
            manager: NDManager,
            list: NDList,
            encoding: Encoding,
            pooling: String,
            wordRange: String,
        ): NDArray {
            val embedding = list["last_hidden_state"]
            val attentionMask = encoding.attentionMask
            val inputAttentionMask = manager.create(attentionMask).toType(DataType.FLOAT32, true)
            return when (pooling) {
                "cls" -> embedding[0]
                "token" -> tokenPool(embedding, inputAttentionMask, wordRange)
                else -> throw AssertionError("Unexpected pooling mode: $pooling")
            }
        }

        fun getWordRange(wordIds: LongArray, middleTokenIndex: Long): String {
            check(middleTokenIndex != -1L) { "middleTokenIndex must not point to special tokens" }
            check(middleTokenIndex in wordIds) {
                "Specified middleTokenIndex=$middleTokenIndex is not in the passed words=${wordIds.contentToString()}"
            }
            var wordRangeStart: Int? = null
            var wordRangeEnd: Int? = null
            for ((index, wordId) in wordIds.withIndex()) {
                if (wordId == middleTokenIndex) {
                    if (wordRangeStart == null) {
                        wordRangeStart = index
                        wordRangeEnd = index + 1
                    } else {
                        wordRangeEnd = index + 1
                    }
                }
            }
            return "$wordRangeStart:$wordRangeEnd"
        }

        private fun tokenPool(embeddings: NDArray, attentionMask: NDArray, wordRange: String): NDArray {
            val shape = embeddings.shape.shape
            val broadcastAttentionMask = attentionMask.expandDims(-1).broadcast(*shape)
            val prod = embeddings.mul(broadcastAttentionMask)
            return prod[wordRange].mean(intArrayOf(0))
        }

        /**
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
