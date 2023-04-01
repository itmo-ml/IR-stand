import ru.itmo.stand.util.kmeans.RandomNumberGenerator

class MersenneTwister : RandomNumberGenerator {
    private val mt = IntArray(N)

    private var mti = 0

    @JvmOverloads
    constructor(seed: Int = MAGIC_SEED) {
        setSeed(seed)
    }

    constructor(seed: Long) {
        setSeed(seed)
    }

    override fun setSeed(seed: Long) {
        // Integer.MAX_VALUE (2,147,483,647) is the 8th Mersenne prime.
        // Therefore, it is good as a modulus for RNGs.
        setSeed((seed % Int.MAX_VALUE).toInt())
    }

    fun setSeed(seed: Int) {
        mt[0] = seed
        mti = 1
        while (mti < N) {
            mt[mti] = MAGIC_FACTOR1 * (mt[mti - 1] xor (mt[mti - 1] ushr 30)) + mti
            mti++
        }
    }

    override fun next(numbits: Int): Int {
        return nextInt() ushr 32 - numbits
    }

    override fun nextDouble(): Double {
        return (nextInt() ushr 1) / Int.MAX_VALUE.toDouble()
    }

    override fun nextDoubles(d: DoubleArray) {
        val n = d.size
        for (i in 0 until n) {
            d[i] = nextDouble()
        }
    }

    override fun nextInt(): Int {
        var x: Int
        var i: Int
        if (mti >= N) {
            // generate N words at one time
            i = 0
            while (i < N - M) {
                x = mt[i] and UPPER_MASK or (mt[i + 1] and LOWER_MASK)
                mt[i] = mt[i + M] xor (x ushr 1) xor MAGIC[x and 0x1]
                i++
            }
            while (i < N - 1) {
                x = mt[i] and UPPER_MASK or (mt[i + 1] and LOWER_MASK)
                mt[i] = mt[i + (M - N)] xor (x ushr 1) xor MAGIC[x and 0x1]
                i++
            }
            x = mt[N - 1] and UPPER_MASK or (mt[0] and LOWER_MASK)
            mt[N - 1] = mt[M - 1] xor (x ushr 1) xor MAGIC[x and 0x1]
            mti = 0
        }
        x = mt[mti++]

        // Tempering
        x = x xor (x ushr 11)
        x = x xor (x shl 7 and MAGIC_MASK1)
        x = x xor (x shl 15 and MAGIC_MASK2)
        x = x xor (x ushr 18)
        return x
    }

    override fun nextInt(n: Int): Int {
        require(n > 0) { "n must be positive" }

        // n is a power of 2
        if (n and -n == n) {
            return (n * next(31).toLong() shr 31).toInt()
        }
        var bits: Int
        var `val`: Int
        do {
            bits = next(31)
            `val` = bits % n
        } while (bits - `val` + (n - 1) < 0)
        return `val`
    }

    override fun nextLong(): Long {
        val x = nextInt().toLong()
        return x shl 32 or nextInt().toLong()
    }

    companion object {
        /** Mask: Most significant 17 bits  */
        private const val UPPER_MASK = -0x80000000

        /** Mask: Least significant 15 bits  */
        private const val LOWER_MASK = 0x7fffffff

        /** Size of the bytes pool.  */
        private const val N = 624

        /** Period second parameter.  */
        private const val M = 397
        private val MAGIC = intArrayOf(0x0, -0x66f74f21)

        /** The factors used in state initialization.  */
        private const val MAGIC_FACTOR1 = 1812433253
        private const val MAGIC_FACTOR2 = 1664525
        private const val MAGIC_FACTOR3 = 1566083941
        private const val MAGIC_MASK1 = -0x62d3a980
        private const val MAGIC_MASK2 = -0x103a0000

        /** The default seed.  */
        private const val MAGIC_SEED = 19650218
    }
}
