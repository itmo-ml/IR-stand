package ru.itmo.stand.util.kmeans

import org.slf4j.LoggerFactory
import java.util.Arrays
import java.util.stream.IntStream

class XMeans(
    distortion: Double,
    centroids: Array<FloatArray>,
    y: IntArray?,
) :
    CentroidClustering<FloatArray, FloatArray>(distortion, centroids, y!!) {
    override fun distance(x: FloatArray, y: FloatArray): Double {
        return MathEx.squaredDistance(x, y)
    }

    companion object {
        private const val serialVersionUID = 2L
        private val logger = LoggerFactory.getLogger(XMeans::class.java)
        private val LOG2PI = Math.log(Math.PI * 2.0)

        @JvmOverloads
        fun fit(data: Array<FloatArray>, kmax: Int, maxIter: Int = 100, tol: Double = 1E-4): XMeans {
            require(kmax >= 2) { "Invalid parameter kmax = $kmax" }
            val n = data.size
            val d = data[0].size
            var k = 1
            val size = IntArray(kmax)
            size[0] = n
            val y = IntArray(n)
            val sum = Array(kmax) { FloatArray(d) }
            val mean = MathEx.colMeans(data)
            var centroids = arrayOf(mean)
            var distortion = Arrays.stream(data).parallel().mapToDouble { x: FloatArray ->
                MathEx.squaredDistance(
                    x,
                    mean,
                )
            }.sum()
            val distortions = DoubleArray(kmax)
            distortions[0] = distortion
            val bbd = BBDTree(data)
            val kmeans = arrayOfNulls<KMeans>(kmax)
            val centers = ArrayList<FloatArray>()
            while (k < kmax) {
                centers.clear()
                val score = DoubleArray(k)
                for (i in 0 until k) {
                    val ni = size[i]
                    // don't split too small cluster. Anyway likelihood estimation
                    // is not accurate in this case.
                    if (ni < 25) {
                        logger.info("Cluster {} too small to split: {} observations", i, ni)
                        score[i] = 0.0
                        kmeans[i] = null
                        continue
                    }
                    val subset = Array(ni) { floatArrayOf() }
                    var j = 0
                    var l = 0
                    while (j < n) {
                        if (y[j] == i) {
                            subset[l++] = data[j]
                        }
                        j++
                    }
                    kmeans[i] = KMeans.fit(subset, 2, maxIter, tol)
                    val newBIC = bic(2, ni, d, kmeans[i]!!.distortion, kmeans[i]!!.size)
                    val oldBIC = bic(ni, d, distortions[i])
                    score[i] = newBIC - oldBIC
                    logger.info(
                        String.format(
                            "Cluster %3d BIC: %12.4f, BIC after split: %12.4f, improvement: %12.4f",
                            i,
                            oldBIC,
                            newBIC,
                            score[i],
                        ),
                    )
                }
                val index = QuickSort.sort(score)
                for (i in 0 until k) {
                    if (score[i] <= 0.0) {
                        centers.add(centroids[index[i]])
                    }
                }
                val m = centers.size
                var i = k
                while (--i >= 0) {
                    if (score[i] > 0) {
                        if (centers.size + i - m + 1 < kmax) {
                            logger.info("Split cluster {}", index[i])
                            centers.add(kmeans[index[i]]!!.centroids[0])
                            centers.add(kmeans[index[i]]!!.centroids[1])
                        } else {
                            centers.add(centroids[index[i]])
                        }
                    }
                }

                // no more split.
                if (centers.size == k) {
                    logger.info("No more split. Finish with {} clusters", k)
                    break
                }
                k = centers.size
                centroids = centers.toTypedArray()
                var diff = Double.MAX_VALUE
                var iter = 1
                while (iter <= maxIter && diff > tol) {
                    val wcss = bbd.clustering(centroids, sum, size, y)
                    diff = distortion - wcss
                    distortion = wcss
                    iter++
                }
                Arrays.fill(distortions, 0.0)
                IntStream.range(0, k).parallel().forEach { cluster: Int ->
                    val centroid = centers[cluster]
                    for (i in 0 until n) {
                        if (y[i] == cluster) {
                            distortions[cluster] += MathEx.squaredDistance(data[i], centroid)
                        }
                    }
                }
                logger.info(String.format("Distortion with %d clusters: %.5f", k, distortion))
            }
            return XMeans(distortion, centroids, y)
        }

        private fun bic(n: Int, d: Int, distortion: Double): Double {
            val variance = distortion / (n - 1)
            val p1 = -n * LOG2PI
            val p2 = -n * d * Math.log(variance)
            val p3 = -(n - 1).toDouble()
            val L = (p1 + p2 + p3) / 2
            val numParameters = d + 1
            return L - 0.5 * numParameters * Math.log(n.toDouble())
        }

        private fun bic(k: Int, n: Int, d: Int, distortion: Double, clusterSize: IntArray): Double {
            val variance = distortion / (n - k)
            var L = 0.0
            for (i in 0 until k) {
                L += logLikelihood(k, n, clusterSize[i], d, variance)
            }
            val numParameters = k + k * d
            return L - 0.5 * numParameters * Math.log(n.toDouble())
        }

        private fun logLikelihood(k: Int, n: Int, ni: Int, d: Int, variance: Double): Double {
            val p1 = -ni * LOG2PI
            val p2 = -ni * d * Math.log(variance)
            val p3 = -(ni - k).toDouble()
            val p4 = ni * Math.log(ni.toDouble())
            val p5 = -ni * Math.log(n.toDouble())
            return (p1 + p2 + p3) / 2 + p4 + p5
        }
    }
}
