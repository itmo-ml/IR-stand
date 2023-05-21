package ru.itmo.stand.util.kmeans

import java.util.*

class BBDTree(data: Array<FloatArray>) {
    internal class Node(d: Int) {
        var size = 0

        var index = 0
        var center: FloatArray

        var radius: FloatArray

        var sum: FloatArray

        var cost = 0.0

        var lower: Node? = null
        var upper: Node? = null

        init {
            center = FloatArray(d)
            radius = FloatArray(d)
            sum = FloatArray(d)
        }
    }

    private val root: Node

    private val index: IntArray

    init {
        val n = data.size
        index = IntArray(n)
        for (i in 0 until n) {
            index[i] = i
        }

        // Build the tree
        root = buildNode(data, 0, n)
    }

    private fun buildNode(data: Array<FloatArray>, begin: Int, end: Int): Node {
        val d = data[0].size

        // Allocate the node
        val node = Node(d)

        // Fill in basic info
        node.size = end - begin
        node.index = begin

        // Calculate the bounding box
        val lowerBound = FloatArray(d)
        val upperBound = FloatArray(d)
        for (i in 0 until d) {
            lowerBound[i] = data[index[begin]][i]
            upperBound[i] = data[index[begin]][i]
        }
        for (i in begin + 1 until end) {
            for (j in 0 until d) {
                val c = data[index[i]][j]
                if (lowerBound[j] > c) {
                    lowerBound[j] = c
                }
                if (upperBound[j] < c) {
                    upperBound[j] = c
                }
            }
        }

        // Calculate bounding box stats
        var maxRadius = -1.0f
        var splitIndex = -1
        for (i in 0 until d) {
            node.center[i] = (lowerBound[i] + upperBound[i]) / 2
            node.radius[i] = (upperBound[i] - lowerBound[i]) / 2
            if (node.radius[i] > maxRadius) {
                maxRadius = node.radius[i]
                splitIndex = i
            }
        }

        // If the max spread is 0, make this a leaf node
        if (maxRadius < 1E-10) {
            node.upper = null
            node.lower = node.upper
            System.arraycopy(data[index[begin]], 0, node.sum, 0, d)
            if (end > begin + 1) {
                val len = end - begin
                for (i in 0 until d) {
                    node.sum[i] *= len.toFloat()
                }
            }
            node.cost = 0.0
            return node
        }

        // Partition the data around the midpoint in this dimension. The
        // partitioning is done in-place by iterating from left-to-right and
        // right-to-left in the same way that partitioning is done in quicksort.
        val splitCutoff = node.center[splitIndex]
        var i1 = begin
        var i2 = end - 1
        var size = 0
        while (i1 <= i2) {
            var i1Good = data[index[i1]][splitIndex] < splitCutoff
            var i2Good = data[index[i2]][splitIndex] >= splitCutoff
            if (!i1Good && !i2Good) {
                val temp = index[i1]
                index[i1] = index[i2]
                index[i2] = temp
                i2Good = true
                i1Good = i2Good
            }
            if (i1Good) {
                i1++
                size++
            }
            if (i2Good) {
                i2--
            }
        }

        // Create the child nodes
        node.lower = buildNode(data, begin, begin + size)
        node.upper = buildNode(data, begin + size, end)

        // Calculate the new sum and opt cost
        for (i in 0 until d) {
            node.sum[i] = node.lower!!.sum[i] + node.upper!!.sum[i]
        }
        val mean = FloatArray(d)
        for (i in 0 until d) {
            mean[i] = node.sum[i] / node.size
        }
        node.cost = getNodeCost(node.lower, mean) + getNodeCost(node.upper, mean)
        return node
    }

    private fun getNodeCost(node: Node?, center: FloatArray): Double {
        val d = center.size
        var scatter = 0.0
        for (i in 0 until d) {
            val x = node!!.sum[i] / node.size - center[i]
            scatter += x * x
        }
        return node!!.cost + node.size * scatter
    }

    fun clustering(centroids: Array<FloatArray>, sum: Array<FloatArray>, size: IntArray, y: IntArray): Double {
        val k = centroids.size
        Arrays.fill(size, 0)
        val candidates = IntArray(k)
        for (i in 0 until k) {
            candidates[i] = i
            Arrays.fill(sum[i], 0.0f)
        }
        val wcss = filter(root, centroids, candidates, k, sum, size, y)
        val d = centroids[0].size
        for (i in 0 until k) {
            if (size[i] > 0) {
                for (j in 0 until d) {
                    centroids[i][j] = sum[i][j] / size[i]
                }
            }
        }
        return wcss
    }

    private fun filter(
        node: Node?,
        centroids: Array<FloatArray>,
        candidates: IntArray,
        k: Int,
        sum: Array<FloatArray>,
        size: IntArray,
        y: IntArray,
    ): Double {
        val d = centroids[0].size

        // Determine which mean the node mean is closest to
        var minDist = MathEx.squaredDistance(node!!.center, centroids[candidates[0]])
        var closest = candidates[0]
        for (i in 1 until k) {
            val dist = MathEx.squaredDistance(node.center, centroids[candidates[i]])
            if (dist < minDist) {
                minDist = dist
                closest = candidates[i]
            }
        }

        // If this is a non-leaf node, recurse if necessary
        if (node.lower != null) {
            // Build the new list of candidates
            val newCandidates = IntArray(k)
            var k2 = 0
            for (i in 0 until k) {
                if (!prune(node.center, node.radius, centroids, closest, candidates[i])) {
                    newCandidates[k2++] = candidates[i]
                }
            }

            // Recurse if there's at least two
            if (k2 > 1) {
                return filter(node.lower, centroids, newCandidates, k2, sum, size, y) + filter(
                    node.upper,
                    centroids,
                    newCandidates,
                    k2,
                    sum,
                    size,
                    y,
                )
            }
        }

        // Assigns all data within this node to a single mean
        for (i in 0 until d) {
            sum[closest][i] += node.sum[i]
        }
        size[closest] += node.size
        val last = node.index + node.size
        for (i in node.index until last) {
            y[index[i]] = closest
        }
        return getNodeCost(node, centroids[closest])
    }

    private fun prune(
        center: FloatArray,
        radius: FloatArray,
        centroids: Array<FloatArray>,
        bestIndex: Int,
        testIndex: Int,
    ): Boolean {
        if (bestIndex == testIndex) {
            return false
        }
        val d = centroids[0].size
        val best = centroids[bestIndex]
        val test = centroids[testIndex]
        var lhs = 0.0
        var rhs = 0.0
        for (i in 0 until d) {
            val diff = test[i] - best[i]
            lhs += diff * diff
            rhs += if (diff > 0) {
                (center[i] + radius[i] - best[i]) * diff
            } else {
                (center[i] - radius[i] - best[i]) * diff
            }
        }
        return lhs >= 2 * rhs
    }
}
