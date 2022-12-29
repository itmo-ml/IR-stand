package ru.itmo.stand.service.preprocessing

import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.StopFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class StopWordRemoverBenchmark {

    private lateinit var stopWords: Set<String>
    private lateinit var data: String

    // Lucene classes
    private lateinit var tokenizer: StandardTokenizer
    private lateinit var tokenStream: TokenStream
    private lateinit var stopWordSet: CharArraySet
    private lateinit var charTermAttribute: CharTermAttribute

    @Setup
    fun setup() {
        val stopWordsPath = "src/main/resources/data/stopwords.txt"
        stopWords = Files.lines(Paths.get(stopWordsPath)).toList().toSet()
        data = ("Definition of Central nervous system (CNS) Central nervous system (CNS): " +
            "The central nervous system is that part of the nervous system that consists " +
            "of the brain and spinal cord. The central nervous system (CNS) is one of the " +
            "two major divisions of the nervous system. " +
            "The other is the peripheral nervous system (PNS) which is outside the brain and spinal cord. " +
            "The peripheral nervous system (PNS) connects the central nervous system (CNS) " +
            "to sensory organs (such as the eye and ear), other organs of the body, muscles, " +
            "blood vessels and glands.").lowercase()
        // Lucene classes
        tokenizer = StandardTokenizer()
        stopWordSet = CharArraySet(stopWords.size, true)
        stopWordSet.addAll(stopWords)
        tokenStream = StopFilter(tokenizer, stopWordSet)
        charTermAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)
    }

    @Benchmark
    fun removeManually(): List<String> {
        val allWords = data.split(" ").toMutableList()
        return allWords.filter { !stopWords.contains(it) }
    }

    @Benchmark
    fun removeAll(): List<String> {
        val allWords = data.split(" ").toMutableList()
        allWords.removeAll(stopWords)
        return allWords
    }

    @Benchmark
    fun lucene(): List<String> {
        tokenizer.setReader(StringReader(data))
        tokenStream.reset()
        val allWords = mutableListOf<String>()
        while (tokenStream.incrementToken()) {
            val term = charTermAttribute.toString()
            allWords.add(term)
        }
        tokenStream.close()
        return allWords
    }
}
