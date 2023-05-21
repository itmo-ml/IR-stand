package ru.itmo.stand.util.kmeans

import MersenneTwister
import UniversalGenerator

class Random {
    private val real: UniversalGenerator
    private val twister: MersenneTwister

    constructor() {
        real = UniversalGenerator()
        twister = MersenneTwister()
    }

    constructor(seed: Long) {
        real = UniversalGenerator(seed)
        twister = MersenneTwister(seed)
    }

    fun nextDouble(): Double {
        return real.nextDouble()
    }
    fun nextDoubles(d: DoubleArray) {
        real.nextDoubles(d)
    }
    fun nextDouble(lo: Double, hi: Double): Double {
        return lo + (hi - lo) * nextDouble()
    }
    fun nextDoubles(d: DoubleArray, lo: Double, hi: Double) {
        real.nextDoubles(d)
        val l = hi - lo
        val n = d.size
        for (i in 0 until n) {
            d[i] = lo + l * d[i]
        }
    }
    fun nextInt(n: Int): Int {
        return twister.nextInt(n)
    }
}
