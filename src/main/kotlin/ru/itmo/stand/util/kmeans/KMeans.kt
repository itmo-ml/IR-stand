package ru.itmo.stand.util.kmeans


open class KMeans(
    distortion: Double,
    centroids: Array<FloatArray>,
    y: IntArray,
) :
    CentroidClustering<FloatArray, FloatArray>(distortion, centroids, y) {
    public override fun distance(x: FloatArray, y: FloatArray): Double {
        return MathEx.squaredDistance(x, y)
    }

    companion object {
        private const val serialVersionUID = 2L

        @JvmOverloads
        fun fit(data: Array<FloatArray>, k: Int, maxIter: Int = 100, tol: Double = 1E-4): KMeans {
            return fit(BBDTree(data), data, k, maxIter, tol)
        }

        fun fit(bbd: BBDTree, data: Array<FloatArray>, k: Int, maxIter: Int, tol: Double): KMeans {
            require(k >= 2) { "Invalid number of clusters: $k" }
            require(maxIter > 0) { "Invalid maximum number of iterations: $maxIter" }
            val n = data.size
            val d = data[0].size
            val y = IntArray(n)
            val medoids = Array(k) { floatArrayOf() }

            var distortion: Double = MathEx.sum(seed(data, medoids, y, MathEx::squaredDistance))
            //logger.info(String.format("Distortion after initialization: %.4f", distortion))

            // Initialize the centroids
            val size = IntArray(k)
            val centroids = Array(k) {
                FloatArray(
                    d,
                )
            }
            updateCentroids(centroids, data, y, size)
            val sum = Array(k) { FloatArray(d) }
            var diff = Double.MAX_VALUE
            var iter = 1
            while (iter <= maxIter && diff > tol) {
                val wcss: Double = bbd.clustering(centroids, sum, size, y)
                //logger.info(String.format("Distortion after %3d iterations: %.4f", iter, wcss))
                diff = distortion - wcss
                distortion = wcss
                iter++
            }
            return KMeans(distortion, centroids, y)
        }

        @JvmOverloads
        fun lloyd(data: Array<FloatArray>, k: Int, maxIter: Int = 100, tol: Double = 1E-4): KMeans {
            require(k >= 2) { "Invalid number of clusters: $k" }
            require(maxIter > 0) { "Invalid maximum number of iterations: $maxIter" }
            val n = data.size
            val d = data[0].size
            val y = IntArray(n)
            val medoids = Array(k) { floatArrayOf() }
            var distortion: Double = MathEx.sum(seed(data, medoids, y, MathEx::squaredDistanceWithMissingValues))
            //logger.info(String.format("Distortion after initialization: %.4f", distortion))
            val size = IntArray(k)
            val centroids = Array(k) {
                FloatArray(
                    d,
                )
            }
            // The number of non-missing values per cluster per variable.
            val notNaN = Array(k) { IntArray(d) }
            var diff = Double.MAX_VALUE
            var iter = 1
            while (iter <= maxIter && diff > tol) {
                updateCentroidsWithMissingValues(centroids, data, y, size, notNaN)
                val wcss: Double = assign(y, data, centroids, MathEx::squaredDistanceWithMissingValues)
                //logger.info(String.format("Distortion after %3d iterations: %.4f", iter, wcss))
                diff = distortion - wcss
                distortion = wcss
                iter++
            }

            // In case of early stop, we should recalculate centroids.
            if (diff > tol) {
                updateCentroidsWithMissingValues(centroids, data, y, size, notNaN)
            }
            return object : KMeans(distortion, centroids, y) {
                override fun distance(x: FloatArray, y: FloatArray): Double {
                    return MathEx.squaredDistanceWithMissingValues(x, y)
                }
            }
        }
    }
}
