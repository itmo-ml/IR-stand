package ru.itmo.stand.service.preprocessing

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class StopWordRemover : Preprocessor<List<String>, List<String>> {

    private val log = LoggerFactory.getLogger(javaClass)
    private val stopWords: Set<String> = this::class.java.classLoader.getResourceAsStream("./data/stopwords.txt")
        ?.bufferedReader()
        ?.lines()
        ?.collect(Collectors.toSet())
        ?.also { log.info("Stop words are loaded.") }
        ?: error("Could not load stop words [resources:./data/stopwords.txt]")

    override fun preprocess(input: List<String>): List<String> {
        val output = input.toMutableList()
        output.removeAll(stopWords)
        return output
    }
}
