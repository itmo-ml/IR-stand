package ru.itmo.stand.util.kmeans

import java.io.Serializable
import java.util.*
import java.util.function.Supplier
import java.util.function.ToDoubleBiFunction
import java.util.stream.IntStream

abstract class PartitionClustering(
    val k: Int,
    val y: IntArray,
) : Serializable {
    val size: IntArray = IntArray(k + 1)
    init {
        for (yi in y) {
            if (yi == OUTLIER) {
                size[k]++
            } else {
                size[yi]++
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(String.format("Cluster size of %d data points:%n", y.size))
        for (i in 0 until k) {
            val r = 100.0 * size[i] / y.size
            sb.append(String.format("Cluster %4d %6d (%4.1f%%)%n", i + 1, size[i], r))
        }
        if (size[k] != 0) {
            val r = 100.0 * size[k] / y.size
            sb.append(String.format("Outliers     %6d (%4.1f%%)%n", size[k], r))
        }
        return sb.toString()
    }

    companion object {

        const val OUTLIER = Int.MAX_VALUE

        fun <T> seed(data: Array<T>, medoids: Array<T>, y: IntArray, distance: ToDoubleBiFunction<T, T>): DoubleArray {
            val n = data.size
            val k = medoids.size
            val d = DoubleArray(n)
            medoids[0] = data[MathEx.randomInt(n)]
            Arrays.fill(d, Double.MAX_VALUE)

            // pick the next center
            for (j in 1..k) {
                val prev = j - 1
                val medoid = medoids[prev]
                // Loop over the observations and compare them to the most recent center.  Store
                // the distance from each observation to its closest center in scores.
                IntStream.range(0, n).parallel().forEach { i: Int ->
                    // compute the distance between this observation and the current center
                    val dist = distance.applyAsDouble(data[i], medoid)
                    if (dist < d[i]) {
                        d[i] = dist
                        y[i] = prev
                    }
                }
                if (j < k) {
                    var cost = 0.0
                    val cutoff: Double = MathEx.random() * MathEx.sum(d)
                    for (index in 0 until n) {
                        cost += d[index]
                        if (cost >= cutoff) {
                            medoids[j] = data[index]
                            break
                        }
                    }
                }
            }
            return d
        }

        fun <T> run(runs: Int, clustering: Supplier<T>): T where T : PartitionClustering?, T : Comparable<T>? {
            require(runs > 0) { "Invalid number of runs: $runs" }
            return IntStream.range(0, runs)
                .mapToObj { run: Int -> clustering.get() }
                .min(Comparator.naturalOrder())
                .get()
        }
    }
}
