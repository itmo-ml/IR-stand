package ru.itmo.stand.service.bert

import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.Vocabulary
import ai.djl.ndarray.NDList
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import org.springframework.stereotype.Service
import ru.itmo.stand.util.UNKNOWN_TOKEN
import ru.itmo.stand.util.getResource
import ru.itmo.stand.util.wrapToClsAndSep

@Service
class BertTranslator : Translator<List<String>, FloatArray> {

    private lateinit var vocabulary: Vocabulary

    override fun prepare(ctx: TranslatorContext) {
        vocabulary = DefaultVocabulary.builder()
            .optMinFrequency(1)
            .addFromTextFile(getResource("./data/bert/vocab.txt"))
            .optUnknownToken(UNKNOWN_TOKEN)
            .build()
    }

    override fun processInput(ctx: TranslatorContext, input: List<String>): NDList {
        val tokens = input.toMutableList()
        tokens.wrapToClsAndSep()
        val indices = tokens.map(vocabulary::getIndex).toLongArray()
        val attentionMask = LongArray(tokens.size) { 1 }

        val manager = ctx.ndManager
        val indicesArray = manager.create(indices)
        val attentionMaskArray = manager.create(attentionMask)

        // The order matters
        return NDList(indicesArray, attentionMaskArray)
    }

    override fun processOutput(ctx: TranslatorContext, list: NDList): FloatArray {
        return list[0].mean(intArrayOf(0)).toFloatArray()
    }
}
