import ai.djl.huggingface.tokenizers.Encoding
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.ndarray.NDList
import ai.djl.translate.Batchifier
import ai.djl.translate.NoBatchifyTranslator
import ai.djl.translate.TranslatorContext

/** The translator for Huggingface text embedding model.  */
class CustomEmbeddingBatchTranslator internal constructor(
    private val tokenizer: HuggingFaceTokenizer,
    private val batchifier: Batchifier,
    private val pooling: String,
    private val normalize: Boolean,
) : NoBatchifyTranslator<Array<String?>?, Array<FloatArray?>?> {
    /** {@inheritDoc}  */
    override fun processInput(ctx: TranslatorContext, input: Array<String?>?): NDList {
        val manager = ctx.ndManager
        val encodings = tokenizer.batchEncode(input)
        ctx.setAttachment("encodings", encodings)
        val batch = arrayOfNulls<NDList>(encodings.size)
        for (i in encodings.indices) {
            batch[i] = encodings[i].toNDList(manager, false)
        }
        return batchifier.batchify(batch)
    }

    /** {@inheritDoc}  */
    override fun processOutput(ctx: TranslatorContext, list: NDList): Array<FloatArray?> {
        val batch = batchifier.unbatchify(list)
        val encoding = ctx.getAttachment("encodings") as Array<Encoding>
        val manager = ctx.ndManager
        val ret = arrayOfNulls<FloatArray>(batch.size)
        for (i in batch.indices) {
            var array = CustomEmbeddingTranslator.processEmbedding(
                manager,
                batch[i],
                encoding[i],
                pooling,
            )
            if (normalize) {
                array = array.normalize(2.0, 0)
            }
            ret[i] = array.toFloatArray()
        }
        return ret
    }
}
