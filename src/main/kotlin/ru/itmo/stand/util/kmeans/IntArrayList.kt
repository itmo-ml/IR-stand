package ru.itmo.stand.util.kmeans

import java.io.Serializable
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

class IntArrayList @JvmOverloads constructor(capacity: Int = 10) : Serializable {
    var data: IntArray
    private var size = 0
    init {
        data = IntArray(capacity)
    }

    override fun toString(): String {
        return Arrays.stream(data).limit(size.toLong()).mapToObj { i: Int ->
            java.lang.String.valueOf(
                i,
            )
        }.collect(Collectors.joining(", ", "[", "]"))
    }
    fun stream(): IntStream {
        return IntStream.of(*data).limit(size.toLong())
    }
    fun ensureCapacity(capacity: Int) {
        if (capacity > data.size) {
            val newCap = Math.max(data.size shl 1, capacity)
            val tmp = IntArray(newCap)
            System.arraycopy(data, 0, tmp, 0, data.size)
            data = tmp
        }
    }
    fun size(): Int {
        return size
    }
    val isEmpty: Boolean

        get() = size == 0
    fun trim() {
        if (data.size > size) {
            data = toArray()
        }
    }
    fun add(`val`: Int) {
        ensureCapacity(size + 1)
        data[size++] = `val`
    }
    fun add(vals: IntArrayList) {
        ensureCapacity(size + vals.size)
        System.arraycopy(vals.data, 0, data, size, vals.size)
        size += vals.size
    }
    fun add(vals: IntArray) {
        ensureCapacity(size + vals.size)
        System.arraycopy(vals, 0, data, size, vals.size)
        size += vals.size
    }
    operator fun get(index: Int): Int {
        return data[index]
    }
    operator fun set(index: Int, `val`: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index.toString())
        }
        data[index] = `val`
    }
    fun clear() {
        size = 0
    }
    fun remove(index: Int): Int {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index.toString())
        }
        val old = get(index)
        if (index == 0) {
            // data at the front
            System.arraycopy(data, 1, data, 0, size - 1)
        } else if (index != size - 1) {
            // data in the middle
            System.arraycopy(data, index + 1, data, index, size - (index + 1))
        }
        size--
        return old
    }

    @JvmOverloads
    fun toArray(dest: IntArray? = null): IntArray {
        var dest = dest
        if (dest == null || dest.size < size()) {
            dest = IntArray(size)
        }
        System.arraycopy(data, 0, dest, 0, size)
        return dest
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
