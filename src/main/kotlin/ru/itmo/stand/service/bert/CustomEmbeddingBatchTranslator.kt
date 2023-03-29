import ai.djl.huggingface.tokenizers.Encoding
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.ndarray.NDList
import ai.djl.translate.Batchifier
import ai.djl.translate.NoBatchifyTranslator
import ai.djl.translate.TranslatorContext
import ru.itmo.stand.service.bert.CustomTranslatorInput

/** The translator for Huggingface text embedding model.  */
class CustomEmbeddingBatchTranslator internal constructor(
    private val tokenizer: HuggingFaceTokenizer,
    private val batchifier: Batchifier,
    private val pooling: String,
    private val normalize: Boolean,
) : NoBatchifyTranslator<Array<CustomTranslatorInput?>?, Array<FloatArray?>?> {

    override fun getBatchifier(): Batchifier? {
        return super.getBatchifier()
    }

    /** {@inheritDoc}  */
    override fun processInput(ctx: TranslatorContext, input: Array<CustomTranslatorInput?>?): NDList {
        val manager = ctx.ndManager
        val encodings = tokenizer.batchEncode(input?.map { it?.window })
        val indices = input?.map { it?.middleTokenIndex }
        ctx.setAttachment("encodings", encodings)
        ctx.setAttachment("indices", indices)

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
        val indices = ctx.getAttachment("indices") as ArrayList<Long>
        val manager = ctx.ndManager
        val ret = arrayOfNulls<FloatArray>(batch.size)
        for (i in batch.indices) {
            var array = CustomEmbeddingTranslator.processEmbedding(
                manager,
                batch[i],
                encoding[i],
                pooling,
                indices[i]
            )
            if (normalize) {
                array = array.normalize(2.0, 0)
            }
            ret[i] = array.toFloatArray()
        }
        return ret
    }
}
