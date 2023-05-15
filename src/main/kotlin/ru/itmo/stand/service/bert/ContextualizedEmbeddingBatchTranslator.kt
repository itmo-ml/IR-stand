package ru.itmo.stand.service.bert

import ai.djl.huggingface.tokenizers.Encoding
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.ndarray.NDList
import ai.djl.translate.Batchifier
import ai.djl.translate.NoBatchifyTranslator
import ai.djl.translate.TranslatorContext
import ru.itmo.stand.service.bert.ContextualizedEmbeddingTranslator.Companion.getWordRange

class ContextualizedEmbeddingBatchTranslator internal constructor(
    private val tokenizer: HuggingFaceTokenizer,
    private val batchifier: Batchifier,
    private val normalize: Boolean,
    private val pooling: String,
) : NoBatchifyTranslator<Array<TranslatorInput>, Array<FloatArray>> {

    override fun processInput(ctx: TranslatorContext, input: Array<TranslatorInput>): NDList {
        val manager = ctx.ndManager
        val encodings = tokenizer.batchEncode(input.map { it.content })
        val middleWordIndexes = input.map { it.middleWordIndex }
        ctx.setAttachment("encodings", encodings)
        ctx.setAttachment("middleWordIndexes", middleWordIndexes)

        val batch = arrayOfNulls<NDList>(encodings.size)
        for (i in encodings.indices) {
            batch[i] = encodings[i].toNDList(manager, false)
        }
        return batchifier.batchify(batch)
    }

    override fun processOutput(ctx: TranslatorContext, list: NDList): Array<FloatArray> {
        val batch = batchifier.unbatchify(list)
        val encoding = ctx.getAttachment("encodings") as Array<Encoding>
        val middleWordIndexes = ctx.getAttachment("middleWordIndexes") as ArrayList<Int>
        val manager = ctx.ndManager
        val ret = Array(batch.size) { floatArrayOf() }
        for (i in batch.indices) {
            var array = ContextualizedEmbeddingTranslator.processEmbedding(
                manager,
                batch[i],
                encoding[i],
                pooling,
                getWordRange(encoding[i].wordIds, middleWordIndexes[i].toLong()),
            )
            if (normalize) {
                array = array.normalize(2.0, 0)
            }
            ret[i] = array.toFloatArray()
        }
        return ret
    }
}
