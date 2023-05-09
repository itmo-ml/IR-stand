package ru.itmo.stand.config

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import io.github.oshai.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
class NlpConfig {

    private val log = KotlinLogging.logger { }

    @Bean
    fun stanfordCoreNlp(): StanfordCoreNLP {
        val props = Properties()
        props.setProperty("annotators", ANNOTATORS)
        props.setProperty(
            "tokenize.options",
            "untokenizable=allDelete,splitAssimilations=false,splitHyphenated=false,splitForwardSlash=false",
        )
        val stanfordCoreNLP = StanfordCoreNLP(props)

        val message = stanfordCoreNLP.processToCoreDocument("StanfordCoreNLP: warmed up. Annotators: $ANNOTATORS")
        log.info(message.toString())

        return stanfordCoreNLP
    }

    companion object {
        const val ANNOTATORS = "tokenize,pos,lemma"
    }
}
