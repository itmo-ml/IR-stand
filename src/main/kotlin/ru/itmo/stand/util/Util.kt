package ru.itmo.stand.util

import java.util.Locale

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
