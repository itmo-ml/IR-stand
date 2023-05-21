package ru.itmo.stand.util.kmeans

import java.lang.Float.isNaN
import java.util.*
import java.util.function.ToDoubleBiFunction
import java.util.stream.IntStream

abstract class CentroidClustering<T, U>(
    val distortion: Double,

    val centroids: Array<T>,
    y: IntArray,
) :
    PartitionClustering(centroids.size, y), Comparable<CentroidClustering<T, U>?> {

    override fun compareTo(other: CentroidClustering<T, U>?): Int {
        return java.lang.Double.compare(distortion, other!!.distortion)
    }

    /**
     * The distance function.
     * @param x an observation.
     * @param y the other observation.
     * @return the distance.
     */
    protected abstract fun distance(x: T, y: U): Double

    /**
     * Classifies a new observation.
     * @param x a new observation.
     * @return the cluster label.
     */
    fun predict(x: U): Int {
        var nearest = Double.MAX_VALUE
        var label = 0
        for (i in 0 until k) {
            val dist = distance(centroids[i], x)
            if (dist < nearest) {
                nearest = dist
                label = i
            }
        }
        return label
    }

    override fun toString(): String {
        return String.format("Cluster distortion: %.5f%n", distortion) + super.toString()
    }

    companion object {
        private const val serialVersionUID = 2L

        /**
         * Assigns each observation to the nearest centroid.
         */
        fun <T> assign(y: IntArray, data: Array<T>, centroids: Array<T>, distance: ToDoubleBiFunction<T, T>): Double {
            val k = centroids.size
            return IntStream.range(0, data.size).parallel().mapToDouble { i: Int ->
                var nearest = Double.MAX_VALUE
                for (j in 0 until k) {
                    val dist = distance.applyAsDouble(data[i], centroids[j])
                    if (nearest > dist) {
                        nearest = dist
                        y[i] = j
                    }
                }
                nearest
            }.sum()
        }

        /**
         * Calculates the new centroids in the new clusters.
         */
        fun updateCentroids(centroids: Array<FloatArray>, data: Array<FloatArray>, y: IntArray, size: IntArray) {
            val n = data.size
            val k = centroids.size
            val d = centroids[0].size
            Arrays.fill(size, 0)
            IntStream.range(0, k).parallel().forEach { cluster: Int ->
                Arrays.fill(centroids[cluster], 0.0f)
                for (i in 0 until n) {
                    if (y[i] == cluster) {
                        size[cluster]++
                        for (j in 0 until d) {
                            centroids[cluster][j] += data[i][j]
                        }
                    }
                }
                for (j in 0 until d) {
                    centroids[cluster][j] /= size[cluster].toFloat()
                }
            }
        }

        /**
         * Calculates the new centroids in the new clusters with missing values.
         * @param notNaN the number of non-missing values per cluster per variable.
         */
        fun updateCentroidsWithMissingValues(
            centroids: Array<FloatArray>,
            data: Array<FloatArray>,
            y: IntArray,
            size: IntArray,
            notNaN: Array<IntArray>,
        ) {
            val n = data.size
            val k = centroids.size
            val d = centroids[0].size
            IntStream.range(0, k).parallel().forEach { cluster: Int ->
                Arrays.fill(centroids[cluster], 0.0f)
                Arrays.fill(notNaN[cluster], 0)
                for (i in 0 until n) {
                    if (y[i] == cluster) {
                        size[cluster]++
                        for (j in 0 until d) {
                            if (!isNaN(data[i][j])) {
                                centroids[cluster][j] += data[i][j]
                                notNaN[cluster][j]++
                            }
                        }
                    }
                }
                for (j in 0 until d) {
                    centroids[cluster][j] /= notNaN[cluster][j].toFloat()
                }
            }
        }
    }
}
