package ru.itmo.stand.config

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class NlpConfig {

    @Bean
    fun stanfordCoreNlp(): StanfordCoreNLP {
        val props = Properties()
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,depparse") //TODO: move to config props
        val stanfordCoreNLP = StanfordCoreNLP(props)
        stanfordCoreNLP.processToCoreDocument("Warm up text.")
        return stanfordCoreNLP
    }

}
