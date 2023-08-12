package ru.itmo.stand.util

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * https://en.wikipedia.org/wiki/Cosine_similarity
 */
infix fun FloatArray.cos(other: FloatArray): Float {
    require(this.size == other.size) {
        "Vectors must have the same length (this.size=${this.size}, other.size=${other.size})"
    }
    var dotProduct = 0f
    var normThis = 0f
    var normOther = 0f
    for (i in indices) {
        dotProduct += this[i] * other[i]
        normThis += this[i].pow(2)
        normOther += other[i].pow(2)
    }
    return dotProduct / sqrt(normThis * normOther)
}

infix fun FloatArray.dot(other: FloatArray): Float {
    var out = 0.0F
    for (i in indices) out += this[i] * other[i]
    return out
}

fun softmax(numbers: FloatArray): FloatArray {
    val sum = numbers.map { exp(it) }.sum()
    return numbers.map { exp(it) / sum }.toFloatArray()
}

fun FloatArray.toDoubleArray(): DoubleArray {
    return this.map { it.toDouble() }.toDoubleArray()
}

fun Array<FloatArray>.toDoubleArray(): Array<DoubleArray> {
    return this.map { it.toDoubleArray() }.toTypedArray()
}

fun DoubleArray.toTypedFloatArray(): Array<Float> = this.map { it.toFloat() }.toTypedArray()

fun DoubleArray.toFloatArray(): FloatArray = this.map { it.toFloat() }.toFloatArray()

fun String.toFloatArray(): FloatArray = this.split(' ')
    .map { it.toFloat() }
    .toFloatArray()
