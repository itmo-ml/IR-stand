package ru.itmo.stand.service.bert

import ai.djl.huggingface.tokenizers.Encoding
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.ndarray.NDList
import ai.djl.translate.Batchifier
import ai.djl.translate.NoBatchifyTranslator
import ai.djl.translate.TranslatorContext

class ContextualizedEmbeddingBatchTranslator internal constructor(
    private val tokenizer: HuggingFaceTokenizer,
    private val batchifier: Batchifier,
    private val normalize: Boolean,
) : NoBatchifyTranslator<Array<TranslatorInput>, Array<FloatArray>> {

    override fun processInput(ctx: TranslatorContext, input: Array<TranslatorInput>): NDList {
        val encodings = tokenizer.batchEncode(input.map { it.content })
        val wordIndexes = input.map { it.wordIndex }
        ctx.setAttachment("encodings", encodings)
        ctx.setAttachment("wordIndexes", wordIndexes)
        val manager = ctx.ndManager
        val batch = Array(encodings.size) { i -> encodings[i].toNDList(manager, false) }
        return batchifier.batchify(batch)
    }

    override fun processOutput(ctx: TranslatorContext, list: NDList): Array<FloatArray> {
        val batch = batchifier.unbatchify(list)
        val encoding = ctx.getAttachment("encodings") as Array<Encoding>
        val wordIndexes = ctx.getAttachment("wordIndexes") as ArrayList<Int>
        val manager = ctx.ndManager
        return Array(batch.size) { i ->
            var array = ContextualizedEmbeddingTranslator.processEmbedding(
                manager,
                batch[i],
                encoding[i],
                wordIndexes[i],
            )
            if (normalize) {
                array = array.normalize(2.0, 0)
            }
            array.toFloatArray()
        }
    }
}
