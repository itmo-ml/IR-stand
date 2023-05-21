package ru.itmo.stand.util.kmeans

import java.io.Serializable
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

class SparseArray : Iterable<SparseArray.Entry?>, Serializable {

    private val index: IntArrayList

    private val value: DoubleArrayList

    inner class Entry(

        private val index: Int,
    ) {
        val i: Int
        val x: Double
        init {
            i = this@SparseArray.index.get(index)
            x = value.get(index)
        }
        fun update(x: Double) {
            value.set(index, x)
        }
        override fun toString(): String {
            return java.lang.String.format("%d:%s", i, Strings.format(x))
        }
    }

    constructor(entries: List<Entry>) {
        index = IntArrayList(entries.size)
        value = DoubleArrayList(entries.size)
        for (e in entries) {
            index.add(e.i)
            value.add(e.x)
        }
    }

    override fun toString(): String {
        return stream().map(Entry::toString).collect(Collectors.joining(", ", "[", "]"))
    }

    fun size(): Int {
        return index.size()
    }

    override fun iterator(): Iterator<Entry> {
        return object : Iterator<Entry> {
            var i = 0
            override fun hasNext(): Boolean {
                return i < size()
            }

            override fun next(): Entry {
                return Entry(i++)
            }
        }
    }

    fun stream(): Stream<Entry> {
        return IntStream.range(0, size()).mapToObj { index: Int -> Entry(index) }
    }

    fun sort() {
        QuickSort.sort(index.data, value.data, size())
    }

    operator fun get(i: Int): Double {
        val length = size()
        for (k in 0 until length) {
            if (index.get(k) === i) return value.get(k)
        }
        return 0.0
    }

    operator fun set(i: Int, x: Double): Boolean {
        if (x == 0.0) {
            remove(i)
            return false
        }
        val length = size()
        for (k in 0 until length) {
            if (index.get(k) === i) {
                value.set(k, x)
                return false
            }
        }
        index.add(i)
        value.add(x)
        return true
    }

    fun append(i: Int, x: Double) {
        if (x != 0.0) {
            index.add(i)
            value.add(x)
        }
    }

    fun remove(i: Int) {
        val length = size()
        for (k in 0 until length) {
            if (index.get(k) === i) {
                index.remove(k)
                value.remove(k)
                return
            }
        }
    }

    companion object {
        private const val serialVersionUID = 2L
    }
}
