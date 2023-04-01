package ru.itmo.stand.util.kmeans

interface QuickSelect {
    companion object {

        fun select(x: IntArray, k: Int): Int {
            val n = x.size
            var l = 0
            var ir = n - 1
            var a: Int
            var i: Int
            var j: Int
            var mid: Int
            while (true) {
                if (ir <= l + 1) {
                    if (ir == l + 1 && x[ir] < x[l]) {
                        Sort.swap(x, l, ir)
                    }
                    return x[k]
                } else {
                    mid = l + ir shr 1
                    Sort.swap(x, mid, l + 1)
                    if (x[l] > x[ir]) {
                        Sort.swap(x, l, ir)
                    }
                    if (x[l + 1] > x[ir]) {
                        Sort.swap(x, l + 1, ir)
                    }
                    if (x[l] > x[l + 1]) {
                        Sort.swap(x, l, l + 1)
                    }
                    i = l + 1
                    j = ir
                    a = x[l + 1]
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
                    }
                    x[l + 1] = x[j]
                    x[j] = a
                    if (j >= k) {
                        ir = j - 1
                    }
                    if (j <= k) {
                        l = i
                    }
                }
            }
        }

        fun select(x: FloatArray, k: Int): Float {
            val n = x.size
            var l = 0
            var ir = n - 1
            var a: Float
            var i: Int
            var j: Int
            var mid: Int
            while (true) {
                if (ir <= l + 1) {
                    if (ir == l + 1 && x[ir] < x[l]) {
                        Sort.swap(x, l, ir)
                    }
                    return x[k]
                } else {
                    mid = l + ir shr 1
                    Sort.swap(x, mid, l + 1)
                    if (x[l] > x[ir]) {
                        Sort.swap(x, l, ir)
                    }
                    if (x[l + 1] > x[ir]) {
                        Sort.swap(x, l + 1, ir)
                    }
                    if (x[l] > x[l + 1]) {
                        Sort.swap(x, l, l + 1)
                    }
                    i = l + 1
                    j = ir
                    a = x[l + 1]
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
                    }
                    x[l + 1] = x[j]
                    x[j] = a
                    if (j >= k) {
                        ir = j - 1
                    }
                    if (j <= k) {
                        l = i
                    }
                }
            }
        }

        fun select(x: DoubleArray, k: Int): Double {
            val n = x.size
            var l = 0
            var ir = n - 1
            var a: Double
            var i: Int
            var j: Int
            var mid: Int
            while (true) {
                if (ir <= l + 1) {
                    if (ir == l + 1 && x[ir] < x[l]) {
                        Sort.swap(x, l, ir)
                    }
                    return x[k]
                } else {
                    mid = l + ir shr 1
                    Sort.swap(x, mid, l + 1)
                    if (x[l] > x[ir]) {
                        Sort.swap(x, l, ir)
                    }
                    if (x[l + 1] > x[ir]) {
                        Sort.swap(x, l + 1, ir)
                    }
                    if (x[l] > x[l + 1]) {
                        Sort.swap(x, l, l + 1)
                    }
                    i = l + 1
                    j = ir
                    a = x[l + 1]
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
                    }
                    x[l + 1] = x[j]
                    x[j] = a
                    if (j >= k) {
                        ir = j - 1
                    }
                    if (j <= k) {
                        l = i
                    }
                }
            }
        }

        fun <T : Comparable<T>?> select(x: Array<T>, k: Int): T {
            val n = x.size
            var l = 0
            var ir = n - 1
            var a: T
            var i: Int
            var j: Int
            var mid: Int
            while (true) {
                if (ir <= l + 1) {
                    if (ir == l + 1 && x[ir]!!.compareTo(x[l]) < 0) {
                        Sort.swap(x, l, ir)
                    }
                    return x[k]
                } else {
                    mid = l + ir shr 1
                    Sort.swap(x, mid, l + 1)
                    if (x[l]!!.compareTo(x[ir]) > 0) {
                        Sort.swap(x, l, ir)
                    }
                    if (x[l + 1]!!.compareTo(x[ir]) > 0) {
                        Sort.swap(x, l + 1, ir)
                    }
                    if (x[l]!!.compareTo(x[l + 1]) > 0) {
                        Sort.swap(x, l, l + 1)
                    }
                    i = l + 1
                    j = ir
                    a = x[l + 1]
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
                    }
                    x[l + 1] = x[j]
                    x[j] = a
                    if (j >= k) {
                        ir = j - 1
                    }
                    if (j <= k) {
                        l = i
                    }
                }
            }
        }
        fun median(x: IntArray): Int {
            val k = x.size / 2
            return select(x, k)
        }
        fun median(x: FloatArray): Float {
            val k = x.size / 2
            return select(x, k)
        }
        fun median(x: DoubleArray): Double {
            val k = x.size / 2
            return select(x, k)
        }

        fun <T : Comparable<T>?> median(x: Array<T>): T {
            val k = x.size / 2
            return select(x, k)
        }

        fun q1(x: IntArray): Int {
            val k = x.size / 4
            return select(x, k)
        }

        fun q1(x: FloatArray): Float {
            val k = x.size / 4
            return select(x, k)
        }
        fun q1(x: DoubleArray): Double {
            val k = x.size / 4
            return select(x, k)
        }
        fun <T : Comparable<T>?> q1(x: Array<T>): T {
            val k = x.size / 4
            return select(x, k)
        }
    }
}
