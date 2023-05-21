import ru.itmo.stand.util.kmeans.RandomNumberGenerator

class UniversalGenerator : RandomNumberGenerator {
    private var c = 0.0
    private var cd = 0.0
    private var cm = 0.0
    private var u: DoubleArray = DoubleArray(0)
    private var i97 = 0
    private var j97 = 0

    constructor() {
        setSeed(DEFAULT_SEED.toLong())
    }

    constructor(seed: Long) {
        setSeed(seed)
    }

    override fun setSeed(seed: Long) {
        u = DoubleArray(97)
        val ijkl = Math.abs((seed % BIG_PRIME).toInt())
        var ij = ijkl / 30082
        var kl = ijkl % 30082

        if (ij < 0 || ij > 31328 || kl < 0 || kl > 30081) {
            ij = ij % 31329
            kl = kl % 30082
        }
        var i = ij / 177 % 177 + 2
        var j = ij % 177 + 2
        var k = kl / 169 % 178 + 1
        var l = kl % 169
        var m: Int
        var s: Double
        var t: Double
        for (ii in 0..96) {
            s = 0.0
            t = 0.5
            for (jj in 0..23) {
                m = i * j % 179 * k % 179
                i = j
                j = k
                k = m
                l = (53 * l + 1) % 169
                if (l * m % 64 >= 32) {
                    s += t
                }
                t *= 0.5
            }
            u[ii] = s
        }
        c = 362436.0 / 16777216.0
        cd = 7654321.0 / 16777216.0
        cm = 16777213.0 / 16777216.0
        i97 = 96
        j97 = 32
    }

    override fun nextDouble(): Double {
        var uni: Double
        uni = u[i97] - u[j97]
        if (uni < 0.0) {
            uni += 1.0
        }
        u[i97] = uni
        if (--i97 < 0) {
            i97 = 96
        }
        if (--j97 < 0) {
            j97 = 96
        }
        c -= cd
        if (c < 0.0) {
            c += cm
        }
        uni -= c
        if (uni < 0.0) {
            uni++
        }
        return uni
    }

    override fun nextDoubles(d: DoubleArray) {
        val n = d.size
        var uni: Double
        for (i in 0 until n) {
            uni = u[i97] - u[j97]
            if (uni < 0.0) {
                uni += 1.0
            }
            u[i97] = uni
            if (--i97 < 0) {
                i97 = 96
            }
            if (--j97 < 0) {
                j97 = 96
            }
            c -= cd
            if (c < 0.0) {
                c += cm
            }
            uni -= c
            if (uni < 0.0) {
                uni += 1.0
            }
            d[i] = uni
        }
    }

    override fun next(numbits: Int): Int {
        return nextInt() ushr 32 - numbits
    }

    override fun nextInt(): Int {
        return Math.floor(Int.MAX_VALUE * (2 * nextDouble() - 1.0)).toInt()
    }

    override fun nextInt(n: Int): Int {
        require(n > 0) { "n must be positive" }
        return (nextDouble() * n).toInt()
    }

    override fun nextLong(): Long {
        return Math.floor(Long.MAX_VALUE * (2 * nextDouble() - 1.0)).toLong()
    }

    companion object {

        private const val DEFAULT_SEED = 54217137

        private const val BIG_PRIME = 899999963
    }
}
