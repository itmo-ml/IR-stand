package ru.itmo.stand.util

import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.bert.WordpieceTokenizer
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.lucene.analysis.shingle.ShingleFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import java.io.StringReader
import java.lang.Integer.min
import java.lang.Math.max
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.Locale
import kotlin.math.exp
import kotlin.system.measureTimeMillis

fun Long.formatBytesToReadable(locale: Locale = Locale.getDefault()): String = when {
    this < 1024 -> "$this B"
    else -> {
        val z = (63 - java.lang.Long.numberOfLeadingZeros(this)) / 10
        String.format(locale, "%.1f %siB", this.toDouble() / (1L shl z * 10), " KMGTPE"[z])
    }
}

infix fun FloatArray.dot(other: FloatArray): Float {
    var out = 0.0F
    for (i in indices) out += this[i] * other[i]
    return out
}

fun String.toNgrams(minGram: Int = 2, maxGram: Int = 2): List<String> {
    val reader = StringReader(this)
    val standardTokenizer = StandardTokenizer().apply { setReader(reader) }
    val shingleFilter = ShingleFilter(standardTokenizer, minGram, maxGram)
    val attr = shingleFilter.addAttribute(CharTermAttribute::class.java).also { shingleFilter.reset() }
    return mutableListOf<String>().apply { while (shingleFilter.incrementToken()) add(attr.toString()) }
}

fun String.toTokens(stanfordCoreNLP: StanfordCoreNLP) : List<String> = stanfordCoreNLP.processToCoreDocument(this)
        .tokens()
        .map { it.lemma().lowercase() }


val vocabulary = emptyList<String>() // TODO: add vocabulary

fun String.toSubWords(): List<String> {
    val wordpieceTokenizer = WordpieceTokenizer(DefaultVocabulary(vocabulary), "[UNK]", Int.MAX_VALUE)
    return wordpieceTokenizer.tokenize(this)
}

// TODO: use measureTime when it's stable
fun measureTimeSeconds(block: () -> Unit): Double = measureTimeMillis(block) / 1000.0

fun walkDirectory(dirPath: Path): List<Path> {
    val paths = mutableListOf<Path>()
    Files.walkFileTree(
        dirPath,
        object : SimpleFileVisitor<Path>() {
            override fun visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult {
                paths.add(path)
                return FileVisitResult.CONTINUE
            }
        }
    )
    return paths
}

fun softmax(numbers: FloatArray): FloatArray {
    val sum = numbers.map { exp(it) }.sum()
    return numbers.map { exp(it) /sum }.toFloatArray()
}



/**
 * For n tokens and size = m,
 * should return slices where each slice with len = m
 * and each token appear in the middle of the window.
 */
fun <T> List<T>.createContexts(size: Int): List<List<T>> {

    throwIf(size % 2 == 0, IllegalArgumentException("Size value should be odd"))
    throwIf(size <= 0, IllegalArgumentException("Size value should be greater than zero"))
    throwIf(size > this.size, IllegalArgumentException("Size value cannot be greater than List size"))
    val sideTokensCount = (size - 1) / 2;
    val partialWindowSize = sideTokensCount + 1
    if(this.size <= sideTokensCount + 1) {
        return arrayListOf(this)
    }
    val result = ArrayList<List<T>>()
    for(index in 0 until sideTokensCount) {
        result.add(this.subList(0, partialWindowSize + index))
    }
    for(index in sideTokensCount until this.size - sideTokensCount) {
        result.add(this.subList(index - sideTokensCount, index + sideTokensCount + 1))
    }
    for(index in this.size - size + 1 until this.size - sideTokensCount) {
        result.add(this.subList(index, this.size))
    }
    return result
}

fun throwIf(condition: Boolean, ex: Exception) {
    if(condition) throw ex;
}
