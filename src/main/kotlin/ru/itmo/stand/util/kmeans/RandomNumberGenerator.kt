package ru.itmo.stand.util.kmeans

interface RandomNumberGenerator {
    fun setSeed(seed: Long)

    fun next(numbits: Int): Int

    fun nextInt(): Int
    fun nextInt(n: Int): Int

    fun nextLong(): Long

    fun nextDouble(): Double

    fun nextDoubles(d: DoubleArray)
}
