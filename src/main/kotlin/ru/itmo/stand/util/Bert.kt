package ru.itmo.stand.util

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.Vocabulary

const val SEP_TOKEN = "[SEP]"
const val CLS_TOKEN = "[CLS]"
const val TOKEN_SEPARATOR = "tokenseparator"
const val UNKNOWN_TOKEN = "[UNK]"

fun MutableList<String>.wrapToClsAndSep() {
    this.add(0, CLS_TOKEN)
    this.add(SEP_TOKEN)
}

val bertVocabulary: Vocabulary = DefaultVocabulary.builder()
    .optMinFrequency(1)
    .addFromTextFile(getResource("./data/bert/vocab.txt"))
    .optUnknownToken(UNKNOWN_TOKEN)
    .build()

val bertTokenizer: HuggingFaceTokenizer = HuggingFaceTokenizer.newInstance(
    getResourceAsStream("./data/bert/tokenizer.json"),
    null,
)
