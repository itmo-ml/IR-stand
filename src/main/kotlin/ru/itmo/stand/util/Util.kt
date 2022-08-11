package ru.itmo.stand.util

import java.io.StringReader
import java.util.Locale
import org.apache.lucene.analysis.shingle.ShingleFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

fun Long.formatBytesToReadable(locale: Locale = Locale.getDefault()): String = when {
    this < 1024 -> "$this B"
    else -> {
        val z = (63 - java.lang.Long.numberOfLeadingZeros(this)) / 10
        String.format(locale, "%.1f %siB", this.toDouble() / (1L shl z * 10), " KMGTPE"[z])
    }
}

infix fun FloatArray.dot(other: FloatArray): Double {
    var out = 0.0
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
