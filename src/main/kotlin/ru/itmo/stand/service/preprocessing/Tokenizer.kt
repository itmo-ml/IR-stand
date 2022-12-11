package ru.itmo.stand.service.preprocessing

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.stereotype.Service
import ru.itmo.stand.util.toTokens

@Service
class Tokenizer(private val stanfordCoreNlp: StanfordCoreNLP) : Preprocessor<String, List<String>> {

    override fun preprocess(input: String): List<String> = input.toTokens(stanfordCoreNlp)
}
