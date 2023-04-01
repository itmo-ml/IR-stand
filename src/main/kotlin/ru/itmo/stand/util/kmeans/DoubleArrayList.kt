package ru.itmo.stand.util.kmeans

import java.io.Serializable
import java.text.DecimalFormat
import java.util.*
import java.util.stream.Collectors
import java.util.stream.DoubleStream

class DoubleArrayList @JvmOverloads constructor(capacity: Int = 10) : Serializable {
    var data: DoubleArray
    private var size = 0
    init {
        data = DoubleArray(capacity)
    }
    override fun toString(): String {
        return Arrays.stream(data).limit(size.toLong()).mapToObj { number: Double ->
            format.format(
                number,
            )
        }.collect(Collectors.joining(", ", "[", "]"))
    }

    fun stream(): DoubleStream {
        return DoubleStream.of(*data).limit(size.toLong())
    }
    fun ensureCapacity(capacity: Int) {
        if (capacity > data.size) {
            val newCap = Math.max(data.size shl 1, capacity)
            val tmp = DoubleArray(newCap)
            System.arraycopy(data, 0, tmp, 0, data.size)
            data = tmp
        }
    }
    fun size(): Int {
        return size
    }

    fun trim() {
        if (data.size > size()) {
            data = toArray()
        }
    }
    fun add(`val`: Double) {
        ensureCapacity(size + 1)
        data[size++] = `val`
    }
    fun add(vals: DoubleArray) {
        ensureCapacity(size + vals.size)
        System.arraycopy(vals, 0, data, size, vals.size)
        size += vals.size
    }
    operator fun get(index: Int): Double {
        return data[index]
    }
    operator fun set(index: Int, `val`: Double) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index.toString())
        }
        data[index] = `val`
    }
    fun clear() {
        size = 0
    }
    fun remove(index: Int): Double {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index.toString())
        }
        val old = get(index)
        if (index == 0) {
            // data at the front
            System.arraycopy(data, 1, data, 0, size - 1)
        } else if (size - 1 != index) {
            // data in the middle
            System.arraycopy(data, index + 1, data, index, size - (index + 1))
        }
        size--
        return old
    }

    @JvmOverloads
    fun toArray(dest: DoubleArray? = null): DoubleArray {
        var dest = dest
        if (dest == null || dest.size < size()) {
            dest = DoubleArray(size)
        }
        System.arraycopy(data, 0, dest, 0, size)
        return dest
    }
    companion object {
        private const val serialVersionUID = 1L

        /** Format for toString.  */
        private val format = DecimalFormat("#.######")
    }
}
