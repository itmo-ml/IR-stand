package ru.itmo.stand.service.impl.bert_nsp

import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.Vocabulary
import ai.djl.modality.nlp.bert.BertTokenizer
import ai.djl.ndarray.NDList
import ai.djl.translate.Batchifier
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Params.BASE_PATH
import ru.itmo.stand.util.softmax
import java.nio.file.Paths
import java.util.Locale
import kotlin.math.exp

@Service
class BertNspTranslator : Translator<String, FloatArray> {

    private lateinit var vocabulary: Vocabulary
    private lateinit var tokenizer: BertTokenizer

    override fun prepare(ctx: TranslatorContext?) {
        val path = Paths.get("$BASE_PATH/data/custom/vocab.txt")
        vocabulary = DefaultVocabulary.builder()
            .optMinFrequency(1)
            .addFromTextFile(path)
            .optUnknownToken("[UNK]")
            .build()
        tokenizer = BertTokenizer()
    }

    override fun processInput(ctx: TranslatorContext, input: String): NDList {
        val tokens = tokenizer.tokenize(input.lowercase(Locale.getDefault()))

        var separatorIndex = tokens.indexOf(TokenSeparator);
        tokens[separatorIndex] = SepToken;
        tokens.add(0, ClsToken)
        tokens.add(SepToken)

        val indices = tokens?.stream()?.mapToLong(vocabulary::getIndex)?.toArray()
        val attentionMask = LongArray(tokens.size) { 1 }

        val manager = ctx.ndManager
        val indicesArray = manager.create(indices)
        val attentionMaskArray = manager.create(attentionMask)

        // The order matters
        return NDList(indicesArray, attentionMaskArray)
    }

    override fun processOutput(ctx: TranslatorContext?, list: NDList): FloatArray {
        return softmax(list[0].toFloatArray());
    }

    override fun getBatchifier(): Batchifier? {
        return Batchifier.STACK
    }
}