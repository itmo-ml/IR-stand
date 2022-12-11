package ru.itmo.stand.config

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class NlpConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun stanfordCoreNlp(): StanfordCoreNLP {
        val props = Properties()
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma")
        val stanfordCoreNLP = StanfordCoreNLP(props)

        val message = stanfordCoreNLP.processToCoreDocument("StanfordCoreNLP: warmed up.")
        log.info(message.toString())

        return stanfordCoreNLP
    }

}
