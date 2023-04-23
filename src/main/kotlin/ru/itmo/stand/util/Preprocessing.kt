package ru.itmo.stand.util

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.shingle.ShingleFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import java.io.IOException
import java.io.StringReader

fun String.toNgrams(minGram: Int = 2, maxGram: Int = 2): List<String> {
    val reader = StringReader(this)
    val standardTokenizer = StandardTokenizer().apply { setReader(reader) }
    val shingleFilter = ShingleFilter(standardTokenizer, minGram, maxGram)
    val attr = shingleFilter.addAttribute(CharTermAttribute::class.java).also { shingleFilter.reset() }
    return mutableListOf<String>().apply { while (shingleFilter.incrementToken()) add(attr.toString()) }
}

fun String.toTokens(stanfordCoreNLP: StanfordCoreNLP): List<String> = stanfordCoreNLP.processToCoreDocument(this)
    .tokens()
    .map { it.lemma().lowercase() }

/**
 * For n tokens and size = m,
 * should return slices where each slice with len = m
 * and each token appear in the middle of the window.
 */
fun List<String>.createWindows(size: Int): List<Window> {
    require(size % 2 != 0) { "Size value should be odd" }
    require(size > 0) { "Size value should be greater than zero" }
    val sideTokensCount = (size - 1) / 2
    val partialWindowSize = sideTokensCount + 1
    if (this.size <= sideTokensCount + 1) {
        return arrayListOf(Window(content = this, tokenIndex = 0))
    }
    val result = mutableListOf<Window>()
    for (index in 0 until sideTokensCount) {
        result.add(Window(this[index], this.subList(0, partialWindowSize + index), index))
    }
    for (index in sideTokensCount until this.size - sideTokensCount) {
        result.add(Window(this[index], this.subList(index - sideTokensCount, index + sideTokensCount + 1), index))
    }
    for (index in this.size - size + 1 until this.size - sideTokensCount) {
        result.add(Window(this[index + sideTokensCount], this.subList(index, this.size), index))
    }
    return result
}

data class Window internal constructor(
    val middleToken: String = "UNKNOWN",
    val content: List<String>,
    val tokenIndex: Int,
) {
    fun convertContentToString() = content.joinToString(" ")
}

fun analyze(analyzer: Analyzer, s: String): List<String> {
    val list = mutableListOf<String>()
    try {
        val tokenStream = analyzer.tokenStream(null, StringReader(s))
        val charAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)
        tokenStream.reset()
        while (tokenStream.incrementToken()) {
            if (charAttribute.toString().isEmpty()) {
                continue
            }
            list.add(charAttribute.toString())
        }
        tokenStream.end()
        tokenStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return list
}
