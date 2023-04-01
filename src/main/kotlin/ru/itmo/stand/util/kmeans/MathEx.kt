package ru.itmo.stand.util.kmeans

import java.lang.Math.exp;
import java.lang.Math.floor;
import java.lang.Math.abs;
import java.lang.Math.sqrt
import java.util.Arrays
import java.security.SecureRandom
import java.util.stream.LongStream

object MathEx {
    private val fpu = FPU()

    val EPSILON = fpu.EPSILON

    private val seedRNG: SecureRandom = SecureRandom()

    private val DEFAULT_SEEDS = longArrayOf(
        -4106602711295138952L, 7872020634117869514L, -1722503517109829138L, -3386820675908254116L,
        -1736715870046201019L, 3854590623768163340L, 4984519038350406438L, 831971085876758331L,
        7131773007627236777L, -3609561992173376238L, -8759399602515137276L, 6192158663294695439L,
        -5656470009161653116L, -7984826214821970800L, -9113192788977418232L, -8979910231410580019L,
        -4619021025191354324L, -5082417586190057466L, -6554946940783144090L, -3610462176018822900L,
        8959796931768911980L, -4251632352234989839L, 4922191169088134258L, -7282805902317830669L,
        3869302430595840919L, 2517690626940415460L, 4056663221614950174L, 6429856319379397738L,
        7298845553914383313L, 8179510284261677971L, 4282994537597585253L, 7300184601511783348L,
        2596703774884172704L, 1089838915342514714L, 4323657609714862439L, 777826126579190548L,
        -1902743089794461140L, -2460431043688989882L, -3261708534465890932L, 4007861469505443778L,
        8067600139237526646L, 5717273542173905853L, 2938568334013652889L, -2972203304739218305L,
        6544901794394958069L, 7013723936758841449L, -4215598453287525312L, -1454689091401951913L,
        -5699280845313829011L, -9147984414924288540L, 5211986845656222459L, -1287642354429721659L,
        -1509334943513011620L, -9000043616528857326L, -2902817511399216571L, -742823064588229527L,
        -4937222449957498789L, -455679889440396397L, -6109470266907575296L, 5515435653880394376L,
        5557224587324997029L, 8904139390487005840L, 6560726276686488510L, 6959949429287621625L,
        -6055733513105375650L, 5762016937143172332L, -9186652929482643329L, -1105816448554330895L,
        -8200377873547841359L, 9107473159863354619L, 3239950546973836199L, -8104429975176305012L,
        3822949195131885242L, -5261390396129824777L, 9176101422921943895L, -5102541493993205418L,
        -1254710019595692814L, -6668066200971989826L, -2118519708589929546L, 5428466612765068681L,
        -6528627776941116598L, -5945449163896244174L, -3293290115918281076L, 6370347300411991230L,
        -7043881693953271167L, 8078993941165238212L, 6894961504641498099L, -8798276497942360228L,
        2276271091333773917L, -7184141741385833013L, -4787502691178107481L, 1255068205351917608L,
        -8644146770023935609L, 5124094110137147339L, 4917075344795488880L, 3423242822219783102L,
        1588924456880980404L, 8515495360312448868L, -5563691320675461929L, -2352238951654504517L,
        -7416919543420127888L, 631412478604690114L, 689144891258712875L, -9001615284848119152L,
        -6275065758899203088L, 8164387857252400515L, -4122060123604826739L, -2016541034210046261L,
        -7178335877193796678L, 3354303106860129181L, 5731595363486898779L, -2874315602397298018L,
        5386746429707619069L, 9036622191596156315L, -7950190733284789459L, -5741691593792426169L,
        -8600462258998065159L, 5460142111961227035L, 276738899508534641L, 2358776514903881139L,
        -837649704945720257L, -3608906204977108245L, 2960825464614526243L, 7339056324843827739L,
        -5709958573878745135L, -5885403829221945248L, 6611935345917126768L, 2588814037559904539L,
    )

    private var nextSeed = -1

    private val random: ThreadLocal<Random> = object : ThreadLocal<Random>() {
        override fun initialValue(): Random {
            synchronized(DEFAULT_SEEDS) {
                // For the first RNG instance, we use the default seed of RNG algorithms.
                if (nextSeed < 0) {
                    nextSeed = 0
                    return Random()
                }
                return if (nextSeed < DEFAULT_SEEDS.size) {
                    Random(DEFAULT_SEEDS[nextSeed++])
                } else {
                    Random(generateSeed())
                }
            }
        }
    }

    fun log(x: Double): Double {
        var y = -690.7755
        if (x > 1E-300) {
            y = Math.log(x)
        }
        return y
    }

    fun equals(a: Double, b: Double): Boolean {
        if (a == b) {
            return true
        }
        val absa: Double = abs(a)
        val absb: Double = abs(b)
        return abs(a - b) <= Math.min(absa, absb) * 2.2204460492503131e-16
    }

    fun pow2(x: Double): Double {
        return x * x
    }

    fun round(x: Double, decimal: Int): Double {
        return if (decimal < 0) {
            Math.round(x / Math.pow(10.0, -decimal.toDouble())) * Math.pow(10.0, -decimal.toDouble())
        } else {
            Math.round(x * Math.pow(10.0, decimal.toDouble())) / Math.pow(10.0, decimal.toDouble())
        }
    }

    fun lfactorial(n: Int): Double {
        require(n >= 0) { String.format("n has to be non-negative: %d", n) }
        var f = 0.0
        for (i in 2..n) {
            f += Math.log(i.toDouble())
        }
        return f
    }

    fun choose(n: Int, k: Int): Double {
        require(!(n < 0 || k < 0)) { String.format("Invalid n = %d, k = %d", n, k) }
        return if (n < k) {
            0.0
        } else {
            floor(0.5 + exp(lchoose(n, k)))
        }
    }

    fun lchoose(n: Int, k: Int): Double {
        require(!(k < 0 || k > n)) { String.format("Invalid n = %d, k = %d", n, k) }
        return lfactorial(n) - lfactorial(k) - lfactorial(n - k)
    }

    fun generateSeed(): Long {
        val bytes = generateSeed(java.lang.Long.BYTES)
        var seed: Long = 0
        for (i in 0 until java.lang.Long.BYTES) {
            seed = seed shl 8
            seed = seed or (bytes[i].toInt() and 0xFF).toLong()
        }
        return seed
    }

    fun generateSeed(numBytes: Int): ByteArray {
        synchronized(seedRNG) { return seedRNG.generateSeed(numBytes) }
    }

    fun seeds(): LongStream {
        return LongStream.generate(MathEx::generateSeed).sequential()
    }

    fun random(prob: DoubleArray): Int {
        val ans = random(prob, 1)
        return ans[0]
    }

    fun random(prob: DoubleArray, n: Int): IntArray {
        // set up alias table
        val q = DoubleArray(prob.size)
        for (i in prob.indices) {
            q[i] = prob[i] * prob.size
        }

        // initialize a with indices
        val a = IntArray(prob.size)
        for (i in prob.indices) {
            a[i] = i
        }

        // set up H and L
        val HL = IntArray(prob.size)
        var head = 0
        var tail = prob.size - 1
        for (i in prob.indices) {
            if (q[i] >= 1.0) {
                HL[head++] = i
            } else {
                HL[tail--] = i
            }
        }
        while (head != 0 && tail != prob.size - 1) {
            val j = HL[tail + 1]
            val k = HL[head - 1]
            a[j] = k
            q[k] += q[j] - 1
            tail++ // remove j from L
            if (q[k] < 1.0) {
                HL[tail--] = k // add k to L
                head-- // remove k
            }
        }

        // generate sample
        val ans = IntArray(n)
        for (i in 0 until n) {
            var rU = random() * prob.size
            val k = rU.toInt()
            rU -= k.toDouble() /* rU becomes rU-[rU] */
            if (rU < q[k]) {
                ans[i] = k
            } else {
                ans[i] = a[k]
            }
        }
        return ans
    }

    fun random(): Double {
        return random.get().nextDouble()
    }

    fun random(n: Int): DoubleArray {
        val x = DoubleArray(n)
        random.get().nextDoubles(x)
        return x
    }

    fun random(lo: Double, hi: Double): Double {
        return random.get().nextDouble(lo, hi)
    }

    fun random(lo: Double, hi: Double, n: Int): DoubleArray {
        val x = DoubleArray(n)
        random.get().nextDoubles(x, lo, hi)
        return x
    }

    fun randomInt(n: Int): Int {
        return random.get().nextInt(n)
    }

    fun c(vararg x: Int): IntArray {
        return x
    }

    fun c(vararg x: Float): FloatArray {
        return x
    }

    fun c(vararg x: Double): DoubleArray {
        return x
    }

    fun c(vararg x: String): Array<out String> {
        return x
    }

    fun c(vararg list: IntArray): IntArray {
        var n = 0
        for (x in list) n += x.size
        val y = IntArray(n)
        var pos = 0
        for (x in list) {
            System.arraycopy(x, 0, y, pos, x.size)
            pos += x.size
        }
        return y
    }

    fun c(vararg list: FloatArray): FloatArray {
        var n = 0
        for (x in list) n += x.size
        val y = FloatArray(n)
        var pos = 0
        for (x in list) {
            System.arraycopy(x, 0, y, pos, x.size)
            pos += x.size
        }
        return y
    }

    fun c(vararg list: DoubleArray): DoubleArray {
        var n = 0
        for (x in list) n += x.size
        val y = DoubleArray(n)
        var pos = 0
        for (x in list) {
            System.arraycopy(x, 0, y, pos, x.size)
            pos += x.size
        }
        return y
    }

    fun c(vararg list: Array<String?>): Array<String?> {
        var n = 0
        for (x in list) n += x.size
        val y = arrayOfNulls<String>(n)
        var pos = 0
        for (x in list) {
            System.arraycopy(x, 0, y, pos, x.size)
            pos += x.size
        }
        return y
    }

    fun <E> slice(data: Array<E>, index: IntArray): Array<E> {
        val n = index.size
        val x = java.lang.reflect.Array.newInstance(data.javaClass.componentType, n) as Array<E>
        for (i in 0 until n) {
            x[i] = data[index[i]]
        }
        return x
    }

    fun slice(data: IntArray, index: IntArray): IntArray {
        val n = index.size
        val x = IntArray(n)
        for (i in 0 until n) {
            x[i] = data[index[i]]
        }
        return x
    }

    fun slice(data: FloatArray, index: IntArray): FloatArray {
        val n = index.size
        val x = FloatArray(n)
        for (i in 0 until n) {
            x[i] = data[index[i]]
        }
        return x
    }

    fun slice(data: DoubleArray, index: IntArray): DoubleArray {
        val n = index.size
        val x = DoubleArray(n)
        for (i in 0 until n) {
            x[i] = data[index[i]]
        }
        return x
    }

    fun contains(polygon: Array<DoubleArray>, point: DoubleArray): Boolean {
        return contains(polygon, point[0], point[1])
    }

    fun contains(polygon: Array<DoubleArray>, x: Double, y: Double): Boolean {
        if (polygon.size <= 2) {
            return false
        }
        var hits = 0
        val n = polygon.size
        var lastx = polygon[n - 1][0]
        var lasty = polygon[n - 1][1]
        var curx: Double
        var cury: Double

        // Walk the edges of the polygon
        var i = 0
        while (i < n) {
            curx = polygon[i][0]
            cury = polygon[i][1]
            if (cury == lasty) {
                lastx = curx
                lasty = cury
                i++
                continue
            }
            var leftx: Double
            if (curx < lastx) {
                if (x >= lastx) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                leftx = curx
            } else {
                if (x >= curx) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                leftx = lastx
            }
            var test1: Double
            var test2: Double
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                if (x < leftx) {
                    hits++
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                test1 = x - curx
                test2 = y - cury
            } else {
                if (y < lasty || y >= cury) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                if (x < leftx) {
                    hits++
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                test1 = x - lastx
                test2 = y - lasty
            }
            if (test1 < test2 / (lasty - cury) * (lastx - curx)) {
                hits++
            }
            lastx = curx
            lasty = cury
            i++
        }
        return hits and 1 != 0
    }

    fun reverse(a: IntArray) {
        var i = 0
        var j = a.size - 1
        while (i < j) {
            Sort.swap(a, i++, j--) // code for swap not shown, but easy enough
        }
    }

    fun reverse(a: FloatArray) {
        var i = 0
        var j = a.size - 1
        while (i < j) {
            Sort.swap(a, i++, j--) // code for swap not shown, but easy enough
        }
    }
    fun reverse(a: DoubleArray) {
        var i = 0
        var j = a.size - 1
        while (i < j) {
            Sort.swap(a, i++, j--) // code for swap not shown, but easy enough
        }
    }

    fun <T> reverse(a: Array<T>) {
        var i = 0
        var j = a.size - 1
        while (i < j) {
            Sort.swap(a, i++, j--)
        }
    }

    fun mode(a: IntArray): Int {
        Arrays.sort(a)
        var mode = -1
        var count = 0
        var currentValue = a[0]
        var currentCount = 1
        for (i in 1 until a.size) {
            if (a[i] != currentValue) {
                if (currentCount > count) {
                    mode = currentValue
                    count = currentCount
                }
                currentValue = a[i]
                currentCount = 1
            } else {
                currentCount++
            }
        }
        if (currentCount > count) {
            mode = currentValue
        }
        return mode
    }

    fun min(a: Int, b: Int, c: Int): Int {
        return Math.min(Math.min(a, b), c)
    }

    fun min(a: Float, b: Float, c: Float): Float {
        return Math.min(Math.min(a, b), c)
    }

    fun min(a: Double, b: Double, c: Double): Double {
        return Math.min(Math.min(a, b), c)
    }

    fun min(a: Int, b: Int, c: Int, d: Int): Int {
        return Math.min(Math.min(Math.min(a, b), c), d)
    }

    fun min(a: Float, b: Float, c: Float, d: Float): Float {
        return Math.min(Math.min(Math.min(a, b), c), d)
    }

    fun min(a: Double, b: Double, c: Double, d: Double): Double {
        return Math.min(Math.min(Math.min(a, b), c), d)
    }

    fun max(a: Int, b: Int, c: Int): Int {
        return Math.max(Math.max(a, b), c)
    }

    fun max(a: Float, b: Float, c: Float): Float {
        return Math.max(Math.max(a, b), c)
    }

    fun max(a: Double, b: Double, c: Double): Double {
        return Math.max(Math.max(a, b), c)
    }

    fun max(a: Int, b: Int, c: Int, d: Int): Int {
        return Math.max(Math.max(Math.max(a, b), c), d)
    }

    fun max(a: Float, b: Float, c: Float, d: Float): Float {
        return Math.max(Math.max(Math.max(a, b), c), d)
    }

    fun max(a: Double, b: Double, c: Double, d: Double): Double {
        return Math.max(Math.max(Math.max(a, b), c), d)
    }

    fun min(x: IntArray): Int {
        var min = x[0]
        for (n in x) {
            if (n < min) {
                min = n
            }
        }
        return min
    }

    fun min(x: FloatArray): Float {
        var min = Float.POSITIVE_INFINITY
        for (n in x) {
            if (n < min) {
                min = n
            }
        }
        return min
    }

    fun min(x: DoubleArray): Double {
        var min = Double.POSITIVE_INFINITY
        for (n in x) {
            if (n < min) {
                min = n
            }
        }
        return min
    }

    fun max(x: IntArray): Int {
        var max = x[0]
        for (n in x) {
            if (n > max) {
                max = n
            }
        }
        return max
    }

    fun max(x: FloatArray): Float {
        var max = Float.NEGATIVE_INFINITY
        for (n in x) {
            if (n > max) {
                max = n
            }
        }
        return max
    }

    fun max(x: DoubleArray): Double {
        var max = Double.NEGATIVE_INFINITY
        for (n in x) {
            if (n > max) {
                max = n
            }
        }
        return max
    }

    fun min(matrix: Array<IntArray>): Int {
        var min = matrix[0][0]
        for (x in matrix) {
            for (y in x) {
                if (min > y) {
                    min = y
                }
            }
        }
        return min
    }

    fun min(matrix: Array<DoubleArray>): Double {
        var min = Double.POSITIVE_INFINITY
        for (x in matrix) {
            for (y in x) {
                if (min > y) {
                    min = y
                }
            }
        }
        return min
    }

    fun max(matrix: Array<IntArray>): Int {
        var max = matrix[0][0]
        for (x in matrix) {
            for (y in x) {
                if (max < y) {
                    max = y
                }
            }
        }
        return max
    }

    fun max(matrix: Array<DoubleArray>): Double {
        var max = Double.NEGATIVE_INFINITY
        for (x in matrix) {
            for (y in x) {
                if (max < y) {
                    max = y
                }
            }
        }
        return max
    }

    fun colMin(matrix: Array<DoubleArray>): DoubleArray {
        val x = DoubleArray(matrix[0].size)
        Arrays.fill(x, Double.POSITIVE_INFINITY)
        for (row in matrix) {
            for (j in x.indices) {
                if (x[j] > row[j]) {
                    x[j] = row[j]
                }
            }
        }
        return x
    }
    fun colMax(matrix: Array<DoubleArray>): DoubleArray {
        val x = DoubleArray(matrix[0].size)
        Arrays.fill(x, Double.NEGATIVE_INFINITY)
        for (row in matrix) {
            for (j in x.indices) {
                if (x[j] < row[j]) {
                    x[j] = row[j]
                }
            }
        }
        return x
    }

    fun colMeans(matrix: Array<DoubleArray>): DoubleArray {
        val x = matrix[0].clone()
        for (i in 1 until matrix.size) {
            for (j in x.indices) {
                x[j] += matrix[i][j]
            }
        }
        scale(1.0 / matrix.size, x)
        return x
    }

    fun sum(x: ByteArray): Int {
        var sum = 0
        for (n in x) {
            sum += n
        }
        return sum
    }

    fun sum(x: IntArray): Long {
        var sum: Long = 0
        for (n in x) {
            sum += n.toLong()
        }
        return sum.toInt().toLong()
    }

    fun sum(x: FloatArray): Double {
        var sum = 0.0
        for (n in x) {
            sum += n.toDouble()
        }
        return sum
    }

    fun sum(x: DoubleArray): Double {
        var sum = 0.0
        for (n in x) {
            sum += n
        }
        return sum
    }

    fun median(x: IntArray): Int {
        return QuickSelect.median(x)
    }

    fun median(x: FloatArray): Float {
        return QuickSelect.median(x)
    }

    fun median(x: DoubleArray): Double {
        return QuickSelect.median(x)
    }

    fun <T : Comparable<T>?> median(x: Array<T>): T {
        return QuickSelect.median(x)
    }

    fun q1(x: IntArray): Int {
        return QuickSelect.q1(x)
    }

    fun q1(x: FloatArray): Float {
        return QuickSelect.q1(x)
    }

    fun q1(x: DoubleArray): Double {
        return QuickSelect.q1(x)
    }

    fun <T : Comparable<T>?> q1(x: Array<T>): T {
        return QuickSelect.q1(x)
    }

    fun mean(x: IntArray): Double {
        return sum(x).toDouble() / x.size
    }

    fun mean(x: FloatArray): Double {
        return sum(x) / x.size
    }

    fun mean(x: DoubleArray): Double {
        return sum(x) / x.size
    }

    fun `var`(x: IntArray): Double {
        require(x.size >= 2) { "Array length is less than 2." }
        var sum = 0.0
        var sumsq = 0.0
        for (xi in x) {
            sum += xi.toDouble()
            sumsq += (xi * xi).toDouble()
        }
        val n = x.size - 1
        return sumsq / n - sum / x.size * (sum / n)
    }

    fun `var`(x: FloatArray): Double {
        require(x.size >= 2) { "Array length is less than 2." }
        var sum = 0.0
        var sumsq = 0.0
        for (xi in x) {
            sum += xi.toDouble()
            sumsq += (xi * xi).toDouble()
        }
        val n = x.size - 1
        return sumsq / n - sum / x.size * (sum / n)
    }

    fun `var`(x: DoubleArray): Double {
        require(x.size >= 2) { "Array length is less than 2." }
        var sum = 0.0
        var sumsq = 0.0
        for (xi in x) {
            sum += xi
            sumsq += xi * xi
        }
        val n = x.size - 1
        return sumsq / n - sum / x.size * (sum / n)
    }

    fun sd(x: IntArray): Double {
        return sqrt(`var`(x))
    }

    fun sd(x: FloatArray): Double {
        return sqrt(`var`(x))
    }

    fun sd(x: DoubleArray): Double {
        return sqrt(`var`(x))
    }

    fun mad(x: IntArray): Double {
        val m = median(x)
        for (i in x.indices) {
            x[i] = abs(x[i] - m)
        }
        return median(x).toDouble()
    }

    fun mad(x: FloatArray): Double {
        val m = median(x)
        for (i in x.indices) {
            x[i] = abs(x[i] - m)
        }
        return median(x).toDouble()
    }

    fun mad(x: DoubleArray): Double {
        val m = median(x)
        for (i in x.indices) {
            x[i] = abs(x[i] - m)
        }
        return median(x)
    }

    fun distance(x: IntArray, y: IntArray): Double {
        return sqrt(squaredDistance(x, y))
    }

    fun distance(x: FloatArray, y: FloatArray): Double {
        return sqrt(squaredDistance(x, y))
    }

    fun distance(x: DoubleArray, y: DoubleArray): Double {
        return sqrt(squaredDistance(x, y))
    }

    fun distance(x: SparseArray, y: SparseArray): Double {
        return sqrt(squaredDistance(x, y))
    }

    fun squaredDistance(x: IntArray, y: IntArray): Double {
        var d = 0.0
        var p1 = 0
        var p2 = 0
        while (p1 < x.size && p2 < y.size) {
            val i1 = x[p1]
            val i2 = y[p2]
            if (i1 == i2) {
                p1++
                p2++
            } else if (i1 > i2) {
                d++
                p2++
            } else {
                d++
                p1++
            }
        }
        d += (x.size - p1).toDouble()
        d += (y.size - p2).toDouble()
        return d
    }

    fun squaredDistance(x: FloatArray, y: FloatArray): Double {
        require(x.size == y.size) { "Input vector sizes are different." }
        when (x.size) {
            2 -> {
                val d0 = x[0].toDouble() - y[0].toDouble()
                val d1 = x[1].toDouble() - y[1].toDouble()
                return d0 * d0 + d1 * d1
            }

            3 -> {
                val d0 = x[0].toDouble() - y[0].toDouble()
                val d1 = x[1].toDouble() - y[1].toDouble()
                val d2 = x[2].toDouble() - y[2].toDouble()
                return d0 * d0 + d1 * d1 + d2 * d2
            }

            4 -> {
                val d0 = x[0].toDouble() - y[0].toDouble()
                val d1 = x[1].toDouble() - y[1].toDouble()
                val d2 = x[2].toDouble() - y[2].toDouble()
                val d3 = x[3].toDouble() - y[3].toDouble()
                return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3
            }
        }
        var sum = 0.0
        for (i in x.indices) {
            // covert x and y for better precision
            val d = x[i].toDouble() - y[i].toDouble()
            sum += d * d
        }
        return sum
    }

    fun squaredDistance(x: DoubleArray, y: DoubleArray): Double {
        require(x.size == y.size) { "Input vector sizes are different." }
        when (x.size) {
            2 -> {
                val d0 = x[0] - y[0]
                val d1 = x[1] - y[1]
                return d0 * d0 + d1 * d1
            }

            3 -> {
                val d0 = x[0] - y[0]
                val d1 = x[1] - y[1]
                val d2 = x[2] - y[2]
                return d0 * d0 + d1 * d1 + d2 * d2
            }

            4 -> {
                val d0 = x[0] - y[0]
                val d1 = x[1] - y[1]
                val d2 = x[2] - y[2]
                val d3 = x[3] - y[3]
                return d0 * d0 + d1 * d1 + d2 * d2 + d3 * d3
            }
        }
        var sum = 0.0
        for (i in x.indices) {
            val d = x[i] - y[i]
            sum += d * d
        }
        return sum
    }

    fun squaredDistance(x: SparseArray, y: SparseArray): Double {
        val it1: Iterator<SparseArray.Entry> = x.iterator()
        val it2: Iterator<SparseArray.Entry> = y.iterator()
        var e1: SparseArray.Entry? = if (it1.hasNext()) it1.next() else null
        var e2: SparseArray.Entry? = if (it2.hasNext()) it2.next() else null
        var sum = 0.0
        while (e1 != null && e2 != null) {
            if (e1.i === e2.i) {
                sum += pow2(e1.x - e2.x)
                e1 = if (it1.hasNext()) it1.next() else null
                e2 = if (it2.hasNext()) it2.next() else null
            } else if (e1.i > e2.i) {
                sum += pow2(e2.x)
                e2 = if (it2.hasNext()) it2.next() else null
            } else {
                sum += pow2(e1.x)
                e1 = if (it1.hasNext()) it1.next() else null
            }
        }
        while (it1.hasNext()) {
            val d: Double = it1.next().x
            sum += d * d
        }
        while (it2.hasNext()) {
            val d: Double = it2.next().x
            sum += d * d
        }
        return sum
    }

    fun squaredDistanceWithMissingValues(x: DoubleArray, y: DoubleArray): Double {
        val n = x.size
        var m = 0
        var dist = 0.0
        for (i in 0 until n) {
            if (!java.lang.Double.isNaN(x[i]) && !java.lang.Double.isNaN(y[i])) {
                m++
                val d = x[i] - y[i]
                dist += d * d
            }
        }
        dist = if (m == 0) {
            Double.MAX_VALUE
        } else {
            n * dist / m
        }
        return dist
    }

    fun entropy(p: DoubleArray): Double {
        var h = 0.0
        for (pi in p) {
            if (pi > 0) {
                h -= pi * Math.log(pi)
            }
        }
        return h
    }

    fun dot(x: IntArray, y: IntArray): Int {
        var sum = 0
        var p1 = 0
        var p2 = 0
        while (p1 < x.size && p2 < y.size) {
            val i1 = x[p1]
            val i2 = y[p2]
            if (i1 == i2) {
                sum++
                p1++
                p2++
            } else if (i1 > i2) {
                p2++
            } else {
                p1++
            }
        }
        return sum
    }

    fun dot(x: FloatArray, y: FloatArray): Float {
        require(x.size == y.size) { "Arrays have different length." }
        var sum = 0.0f
        for (i in x.indices) {
            sum += x[i] * y[i]
        }
        return sum
    }

    fun dot(x: DoubleArray, y: DoubleArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        var sum = 0.0
        for (i in x.indices) {
            sum += x[i] * y[i]
        }
        return sum
    }

    fun dot(x: SparseArray, y: SparseArray): Double {
        val it1: Iterator<SparseArray.Entry> = x.iterator()
        val it2: Iterator<SparseArray.Entry> = y.iterator()
        var e1: SparseArray.Entry? = if (it1.hasNext()) it1.next() else null
        var e2: SparseArray.Entry? = if (it2.hasNext()) it2.next() else null
        var sum = 0.0
        while (e1 != null && e2 != null) {
            if (e1.i === e2.i) {
                sum += e1.x * e2.x
                e1 = if (it1.hasNext()) it1.next() else null
                e2 = if (it2.hasNext()) it2.next() else null
            } else if (e1.i > e2.i) {
                e2 = if (it2.hasNext()) it2.next() else null
            } else {
                e1 = if (it1.hasNext()) it1.next() else null
            }
        }
        return sum
    }

    fun cov(x: IntArray, y: IntArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        require(x.size >= 3) { "array length has to be at least 3." }
        val mx = mean(x)
        val my = mean(y)
        var Sxy = 0.0
        for (i in x.indices) {
            val dx = x[i] - mx
            val dy = y[i] - my
            Sxy += dx * dy
        }
        return Sxy / (x.size - 1)
    }

    fun cov(x: FloatArray, y: FloatArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        require(x.size >= 3) { "array length has to be at least 3." }
        val mx = mean(x)
        val my = mean(y)
        var Sxy = 0.0
        for (i in x.indices) {
            val dx = x[i] - mx
            val dy = y[i] - my
            Sxy += dx * dy
        }
        return Sxy / (x.size - 1)
    }

    fun cov(x: DoubleArray, y: DoubleArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        require(x.size >= 3) { "array length has to be at least 3." }
        val mx = mean(x)
        val my = mean(y)
        var Sxy = 0.0
        for (i in x.indices) {
            val dx = x[i] - mx
            val dy = y[i] - my
            Sxy += dx * dy
        }
        return Sxy / (x.size - 1)
    }

    @JvmOverloads
    fun cov(data: Array<DoubleArray>, mu: DoubleArray = colMeans(data)): Array<DoubleArray> {
        val sigma = Array(data[0].size) {
            DoubleArray(
                data[0].size,
            )
        }
        for (datum in data) {
            for (j in mu.indices) {
                for (k in 0..j) {
                    sigma[j][k] += (datum[j] - mu[j]) * (datum[k] - mu[k])
                }
            }
        }
        val n = data.size - 1
        for (j in mu.indices) {
            for (k in 0..j) {
                sigma[j][k] /= n.toDouble()
                sigma[k][j] = sigma[j][k]
            }
        }
        return sigma
    }

    fun cor(x: IntArray, y: IntArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        require(x.size >= 3) { "array length has to be at least 3." }
        val Sxy = cov(x, y)
        val Sxx = `var`(x)
        val Syy = `var`(y)
        return if (Sxx == 0.0 || Syy == 0.0) {
            Double.NaN
        } else {
            Sxy / sqrt(Sxx * Syy)
        }
    }

    fun cor(x: FloatArray, y: FloatArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        require(x.size >= 3) { "array length has to be at least 3." }
        val Sxy = cov(x, y)
        val Sxx = `var`(x)
        val Syy = `var`(y)
        return if (Sxx == 0.0 || Syy == 0.0) {
            Double.NaN
        } else {
            Sxy / sqrt(Sxx * Syy)
        }
    }

    fun cor(x: DoubleArray, y: DoubleArray): Double {
        require(x.size == y.size) { "Arrays have different length." }
        require(x.size >= 3) { "array length has to be at least 3." }
        val Sxy = cov(x, y)
        val Sxx = `var`(x)
        val Syy = `var`(y)
        return if (Sxx == 0.0 || Syy == 0.0) {
            Double.NaN
        } else {
            Sxy / sqrt(Sxx * Syy)
        }
    }

    @JvmOverloads
    fun cor(data: Array<DoubleArray>, mu: DoubleArray = colMeans(data)): Array<DoubleArray> {
        val sigma = cov(data, mu)
        val n = data[0].size
        val sd = DoubleArray(n)
        for (i in 0 until n) {
            sd[i] = sqrt(sigma[i][i])
        }
        for (i in 0 until n) {
            for (j in 0..i) {
                sigma[i][j] /= sd[i] * sd[j]
                sigma[j][i] = sigma[i][j]
            }
        }
        return sigma
    }

    fun norm2(x: FloatArray): Float {
        var norm = 0.0f
        for (n in x) {
            norm += n * n
        }
        norm = sqrt(norm.toDouble()).toFloat()
        return norm
    }
    fun norm2(x: DoubleArray): Double {
        var norm = 0.0
        for (n in x) {
            norm += n * n
        }
        norm = sqrt(norm)
        return norm
    }

    fun norm(x: FloatArray): Float {
        return norm2(x)
    }

    fun norm(x: DoubleArray): Double {
        return norm2(x)
    }

    fun cos(x: FloatArray, y: FloatArray): Float {
        return dot(x, y) / (norm2(x) * norm2(y))
    }

    fun cos(x: DoubleArray, y: DoubleArray): Double {
        return dot(x, y) / (norm2(x) * norm2(y))
    }

    fun scale(x: Array<DoubleArray>) {
        val n = x.size
        val p = x[0].size
        val min = colMin(x)
        val max = colMax(x)
        for (j in 0 until p) {
            val scale = max[j] - min[j]
            if (!isZero(scale)) {
                for (i in 0 until n) {
                    x[i][j] = (x[i][j] - min[j]) / scale
                }
            } else {
                for (i in 0 until n) {
                    x[i][j] = 0.5
                }
            }
        }
    }

    @JvmOverloads
    fun equals(x: FloatArray, y: FloatArray, epsilon: Float = 1.0E-7f): Boolean {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            if (abs(x[i] - y[i]) > epsilon) {
                return false
            }
        }
        return true
    }

    @JvmOverloads
    fun equals(x: DoubleArray, y: DoubleArray, epsilon: Double = 1.0E-10): Boolean {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        require(epsilon > 0.0) { "Invalid epsilon: $epsilon" }
        for (i in x.indices) {
            if (abs(x[i] - y[i]) > epsilon) {
                return false
            }
        }
        return true
    }

    @JvmOverloads
    fun equals(x: Array<FloatArray>, y: Array<FloatArray>, epsilon: Float = 1.0E-7f): Boolean {
        require(!(x.size != y.size || x[0].size != y[0].size)) {
            String.format(
                "Matrices have different rows: %d x %d vs %d x %d",
                x.size,
                x[0].size,
                y.size,
                y[0].size,
            )
        }
        for (i in x.indices) {
            for (j in x[i].indices) {
                if (abs(x[i][j] - y[i][j]) > epsilon) {
                    return false
                }
            }
        }
        return true
    }

    @JvmOverloads
    fun equals(x: Array<DoubleArray>, y: Array<DoubleArray>, epsilon: Double = 1.0E-10): Boolean {
        require(!(x.size != y.size || x[0].size != y[0].size)) {
            String.format(
                "Matrices have different rows: %d x %d vs %d x %d",
                x.size,
                x[0].size,
                y.size,
                y[0].size,
            )
        }
        require(epsilon > 0.0) { "Invalid epsilon: $epsilon" }
        for (i in x.indices) {
            for (j in x[i].indices) {
                if (abs(x[i][j] - y[i][j]) > epsilon) {
                    return false
                }
            }
        }
        return true
    }

    fun isZero(x: Float, epsilon: Float): Boolean {
        return abs(x) < epsilon
    }

    fun isZero(x: Double): Boolean {
        return isZero(x, EPSILON)
    }

    fun isZero(x: Double, epsilon: Double): Boolean {
        return abs(x) < epsilon
    }

    fun clone(x: Array<IntArray>): Array<IntArray?> {
        val matrix = arrayOfNulls<IntArray>(x.size)
        for (i in x.indices) {
            matrix[i] = x[i].clone()
        }
        return matrix
    }

    fun clone(x: Array<FloatArray>): Array<FloatArray?> {
        val matrix = arrayOfNulls<FloatArray>(x.size)
        for (i in x.indices) {
            matrix[i] = x[i].clone()
        }
        return matrix
    }

    fun clone(x: Array<DoubleArray>): Array<DoubleArray?> {
        val matrix = arrayOfNulls<DoubleArray>(x.size)
        for (i in x.indices) {
            matrix[i] = x[i].clone()
        }
        return matrix
    }

    fun swap(x: IntArray, i: Int, j: Int) {
        val s = x[i]
        x[i] = x[j]
        x[j] = s
    }

    fun swap(x: FloatArray, i: Int, j: Int) {
        val s = x[i]
        x[i] = x[j]
        x[j] = s
    }

    fun swap(x: DoubleArray, i: Int, j: Int) {
        val s = x[i]
        x[i] = x[j]
        x[j] = s
    }

    fun swap(x: Array<Any?>, i: Int, j: Int) {
        val s = x[i]
        x[i] = x[j]
        x[j] = s
    }

    fun swap(x: IntArray, y: IntArray) {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            val s = x[i]
            x[i] = y[i]
            y[i] = s
        }
    }

    fun swap(x: FloatArray, y: FloatArray) {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            val s = x[i]
            x[i] = y[i]
            y[i] = s
        }
    }

    fun swap(x: DoubleArray, y: DoubleArray) {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            val s = x[i]
            x[i] = y[i]
            y[i] = s
        }
    }

    fun <E> swap(x: Array<E>, y: Array<E>) {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            val s = x[i]
            x[i] = y[i]
            y[i] = s
        }
    }

    fun copy(x: Array<IntArray>, y: Array<IntArray>) {
        require(!(x.size != y.size || x[0].size != y[0].size)) {
            String.format(
                "Matrices have different rows: %d x %d vs %d x %d",
                x.size,
                x[0].size,
                y.size,
                y[0].size,
            )
        }
        for (i in x.indices) {
            System.arraycopy(x[i], 0, y[i], 0, x[i].size)
        }
    }

    fun copy(x: Array<FloatArray>, y: Array<FloatArray>) {
        require(!(x.size != y.size || x[0].size != y[0].size)) {
            String.format(
                "Matrices have different rows: %d x %d vs %d x %d",
                x.size,
                x[0].size,
                y.size,
                y[0].size,
            )
        }
        for (i in x.indices) {
            System.arraycopy(x[i], 0, y[i], 0, x[i].size)
        }
    }

    fun copy(x: Array<DoubleArray>, y: Array<DoubleArray>) {
        require(!(x.size != y.size || x[0].size != y[0].size)) {
            String.format(
                "Matrices have different rows: %d x %d vs %d x %d",
                x.size,
                x[0].size,
                y.size,
                y[0].size,
            )
        }
        for (i in x.indices) {
            System.arraycopy(x[i], 0, y[i], 0, x[i].size)
        }
    }
    fun add(y: DoubleArray, x: DoubleArray) {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            y[i] += x[i]
        }
    }

    fun sub(y: DoubleArray, x: DoubleArray) {
        require(x.size == y.size) { String.format("Arrays have different length: x[%d], y[%d]", x.size, y.size) }
        for (i in x.indices) {
            y[i] -= x[i]
        }
    }

    fun scale(a: Double, x: DoubleArray) {
        for (i in x.indices) {
            x[i] *= a
        }
    }

    fun scale(a: Double, x: DoubleArray, y: DoubleArray) {
        for (i in x.indices) {
            y[i] = a * x[i]
        }
    }

    fun pow(x: DoubleArray, n: Double): DoubleArray {
        val y = DoubleArray(x.size)
        for (i in x.indices) y[i] = Math.pow(x[i], n)
        return y
    }

    fun unique(x: IntArray?): IntArray {
        return Arrays.stream(x).distinct().toArray()
    }

    fun sort(x: Array<DoubleArray>): Array<IntArray?> {
        val n = x.size
        val p = x[0].size
        val a = DoubleArray(n)
        val index = arrayOfNulls<IntArray>(p)
        for (j in 0 until p) {
            for (i in 0 until n) {
                a[i] = x[i][j]
            }
            index[j] = QuickSort.sort(a)
        }
        return index
    }

    fun solve(a: DoubleArray, b: DoubleArray, c: DoubleArray, r: DoubleArray): DoubleArray {
        require(b[0] != 0.0) { "Invalid value of b[0] == 0. The equations should be rewritten as a set of order n - 1." }
        val n = a.size
        val u = DoubleArray(n)
        val gam = DoubleArray(n)
        var bet = b[0]
        u[0] = r[0] / bet
        for (j in 1 until n) {
            gam[j] = c[j - 1] / bet
            bet = b[j] - a[j] * gam[j]
            require(bet != 0.0) { "The tridagonal matrix is not of diagonal dominance." }
            u[j] = (r[j] - a[j] * u[j - 1]) / bet
        }
        for (j in n - 2 downTo 0) {
            u[j] -= gam[j + 1] * u[j + 1]
        }
        return u
    }

    private class FPU internal constructor() {
        var RADIX: Int
        var DIGITS: Int
        var ROUND_STYLE: Int
        var MACHEP: Int
        var FLOAT_MACHEP = -23
        var NEGEP: Int
        var EPSILON: Double

        init {
            val beta: Double
            val betain: Double
            val betah: Double
            var a: Double
            var b: Double
            val ZERO: Double
            val ONE: Double
            val TWO: Double
            var temp: Double
            val tempa: Double
            var temp1: Double
            var i: Int
            var itemp: Int
            ONE = 1.0
            TWO = ONE + ONE
            ZERO = ONE - ONE
            a = ONE
            temp1 = ONE
            while (temp1 - ONE == ZERO) {
                a = a + a
                temp = a + ONE
                temp1 = temp - a
            }
            b = ONE
            itemp = 0
            while (itemp == 0) {
                b = b + b
                temp = a + b
                itemp = (temp - a).toInt()
            }
            RADIX = itemp
            beta = RADIX.toDouble()
            DIGITS = 0
            b = ONE
            temp1 = ONE
            while (temp1 - ONE == ZERO) {
                DIGITS = DIGITS + 1
                b = b * beta
                temp = b + ONE
                temp1 = temp - b
            }
            ROUND_STYLE = 0
            betah = beta / TWO
            temp = a + betah
            if (temp - a != ZERO) {
                ROUND_STYLE = 1
            }
            tempa = a + beta
            temp = tempa + betah
            if (ROUND_STYLE == 0 && temp - tempa != ZERO) {
                ROUND_STYLE = 2
            }
            NEGEP = DIGITS + 3
            betain = ONE / beta
            a = ONE
            i = 0
            while (i < NEGEP) {
                a = a * betain
                i++
            }
            b = a
            temp = ONE - a
            while (temp - ONE == ZERO) {
                a = a * beta
                NEGEP = NEGEP - 1
                temp = ONE - a
            }
            NEGEP = -NEGEP
            MACHEP = -DIGITS - 3
            a = b
            temp = ONE + a
            while (temp - ONE == ZERO) {
                a = a * beta
                MACHEP = MACHEP + 1
                temp = ONE + a
            }
            EPSILON = a
        }
    }
}
