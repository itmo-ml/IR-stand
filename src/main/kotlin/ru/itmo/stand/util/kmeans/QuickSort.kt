package ru.itmo.stand.util.kmeans

object QuickSort {
    private const val M = 7
    private const val NSTACK = 64

    fun sort(x: IntArray): IntArray {
        val order = IntArray(x.size)
        for (i in order.indices) {
            order[i] = i
        }
        sort(x, order)
        return order
    }

    @JvmOverloads
    fun sort(x: IntArray, y: IntArray, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Int
        var b: Int
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    @JvmOverloads
    fun sort(x: IntArray, y: DoubleArray, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Int
        var b: Double
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    @JvmOverloads
    fun sort(x: IntArray, y: Array<Any?>, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Int
        var b: Any?
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    fun sort(x: FloatArray): IntArray {
        val order = IntArray(x.size)
        for (i in order.indices) {
            order[i] = i
        }
        sort(x, order)
        return order
    }

    @JvmOverloads
    fun sort(x: FloatArray, y: IntArray, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Float
        var b: Int
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    @JvmOverloads
    fun sort(x: FloatArray, y: FloatArray, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Float
        var b: Float
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    @JvmOverloads
    fun sort(x: FloatArray, y: Array<Any?>, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Float
        var b: Any?
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    fun sort(x: DoubleArray): IntArray {
        val order = IntArray(x.size)
        for (i in order.indices) {
            order[i] = i
        }
        sort(x, order)
        return order
    }

    @JvmOverloads
    fun sort(x: DoubleArray, y: IntArray, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Double
        var b: Int
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    @JvmOverloads
    fun sort(x: DoubleArray, y: DoubleArray, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Double
        var b: Double
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    @JvmOverloads
    fun sort(x: DoubleArray, y: Array<Any?>, n: Int = x.size) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: Double
        var b: Any?
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i] <= a) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l] > x[ir]) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1] > x[ir]) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l] > x[l + 1]) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i] < a)
                    do {
                        j--
                    } while (x[j] > a)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    fun <T : Comparable<T>?> sort(x: Array<T>): IntArray {
        val order = IntArray(x.size)
        for (i in order.indices) {
            order[i] = i
        }
        sort(x, order)
        return order
    }

    fun <T : Comparable<T>?> sort(x: Array<T>, y: IntArray) {
        sort(x, y, x.size)
    }

    fun <T : Comparable<T>?> sort(x: Array<T>, y: IntArray, n: Int) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: T
        var b: Int
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i]!!.compareTo(a) <= 0) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l]!!.compareTo(x[ir]) > 0) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1]!!.compareTo(x[ir]) > 0) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l]!!.compareTo(x[l + 1]) > 0) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i]!!.compareTo(a) < 0)
                    do {
                        j--
                    } while (x[j]!!.compareTo(a) > 0)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    fun <T> sort(x: Array<T>, y: IntArray, n: Int, comparator: Comparator<T>) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: T
        var b: Int
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (comparator.compare(x[i], a) <= 0) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (comparator.compare(x[l], x[ir]) > 0) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (comparator.compare(x[l + 1], x[ir]) > 0) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (comparator.compare(x[l], x[l + 1]) > 0) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (comparator.compare(x[i], a) < 0)
                    do {
                        j--
                    } while (comparator.compare(x[j], a) > 0)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }

    fun <T : Comparable<T>?> sort(x: Array<T>, y: Array<Any?>) {
        sort(x, y, x.size)
    }

    fun <T : Comparable<T>?> sort(x: Array<T>, y: Array<Any?>, n: Int) {
        var jstack = -1
        var l = 0
        val istack = IntArray(NSTACK)
        var ir = n - 1
        var i: Int
        var j: Int
        var k: Int
        var a: T
        var b: Any?
        while (true) {
            if (ir - l < M) {
                j = l + 1
                while (j <= ir) {
                    a = x[j]
                    b = y[j]
                    i = j - 1
                    while (i >= l) {
                        if (x[i]!!.compareTo(a) <= 0) {
                            break
                        }
                        x[i + 1] = x[i]
                        y[i + 1] = y[i]
                        i--
                    }
                    x[i + 1] = a
                    y[i + 1] = b
                    j++
                }
                if (jstack < 0) {
                    break
                }
                ir = istack[jstack--]
                l = istack[jstack--]
            } else {
                k = l + ir shr 1
                Sort.swap(x, k, l + 1)
                Sort.swap(y, k, l + 1)
                if (x[l]!!.compareTo(x[ir]) > 0) {
                    Sort.swap(x, l, ir)
                    Sort.swap(y, l, ir)
                }
                if (x[l + 1]!!.compareTo(x[ir]) > 0) {
                    Sort.swap(x, l + 1, ir)
                    Sort.swap(y, l + 1, ir)
                }
                if (x[l]!!.compareTo(x[l + 1]) > 0) {
                    Sort.swap(x, l, l + 1)
                    Sort.swap(y, l, l + 1)
                }
                i = l + 1
                j = ir
                a = x[l + 1]
                b = y[l + 1]
                while (true) {
                    do {
                        i++
                    } while (x[i]!!.compareTo(a) < 0)
                    do {
                        j--
                    } while (x[j]!!.compareTo(a) > 0)
                    if (j < i) {
                        break
                    }
                    Sort.swap(x, i, j)
                    Sort.swap(y, i, j)
                }
                x[l + 1] = x[j]
                x[j] = a
                y[l + 1] = y[j]
                y[j] = b
                jstack += 2
                check(jstack < NSTACK) { "NSTACK too small in sort." }
                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir
                    istack[jstack - 1] = i
                    ir = j - 1
                } else {
                    istack[jstack] = j - 1
                    istack[jstack - 1] = l
                    l = i
                }
            }
        }
    }
}
