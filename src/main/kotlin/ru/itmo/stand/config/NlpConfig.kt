package ru.itmo.stand.config

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class NlpConfig {

    private val log = LoggerFactory.getLogger(NlpConfig::class.java)

    @Bean
    fun stanfordCoreNlp(): StanfordCoreNLP {
        val props = Properties()
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma") //TODO: move to config props
        val stanfordCoreNLP = StanfordCoreNLP(props)

        val message = stanfordCoreNLP.processToCoreDocument("StanfordCoreNLP: warmed up.")
        log.info(message.toString())

        return stanfordCoreNLP
    }

}
