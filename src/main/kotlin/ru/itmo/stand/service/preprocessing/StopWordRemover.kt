package ru.itmo.stand.service.preprocessing

import io.github.oshai.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.stand.util.getResourceAsStream
import java.util.stream.Collectors

@Service
class StopWordRemover : Preprocessor<List<String>, List<String>> {

    private val log = KotlinLogging.logger { }
    private val stopWords: Set<String> = getResourceAsStream("/data/stopwords.txt")
        .bufferedReader()
        .lines()
        .collect(Collectors.toSet())
        .also { log.info("Stop words are loaded") }

    override fun preprocess(input: List<String>): List<String> {
        val output = input.toMutableList()
        output.removeAll(stopWords)
        return output
    }
}
