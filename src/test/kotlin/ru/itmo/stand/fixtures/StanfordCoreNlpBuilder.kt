package ru.itmo.stand.fixtures

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import ru.itmo.stand.config.NlpConfig
import java.util.Properties

fun stanfordCoreNLP(): StanfordCoreNLP =
    StanfordCoreNLP(Properties().apply { setProperty("annotators", NlpConfig.ANNOTATORS) })
