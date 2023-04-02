package ru.itmo.stand.util.kmeans

interface Sort {
    companion object {
        fun swap(x: IntArray, i: Int, j: Int) {
            val a = x[i]
            x[i] = x[j]
            x[j] = a
        }

        fun swap(x: FloatArray, i: Int, j: Int) {
            val a = x[i]
            x[i] = x[j]
            x[j] = a
        }
        fun swap(x: DoubleArray, i: Int, j: Int) {
            val a: Double
            a = x[i]
            x[i] = x[j]
            x[j] = a
        }


        fun <T> swap(x: Array<T>, i: Int, j: Int) {
            val a: T
            a = x[i]
            x[i] = x[j]
            x[j] = a
        }
    }
}
