package ru.itmo.stand.util

infix fun FloatArray.dot(other: FloatArray): Double {
    var out = 0.0
    for (i in indices) out += this[i] * other[i]
    return out
}
