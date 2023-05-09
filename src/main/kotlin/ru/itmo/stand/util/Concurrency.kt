package ru.itmo.stand.util

import io.github.oshai.KLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun <T> processParallel(
    data: Sequence<T>,
    numWorkers: Int,
    log: KLogger,
    action: (T) -> Unit,
): Unit = runBlocking(Dispatchers.Default) {
    data
        .onEachIndexed { index, _ -> if (index % 10 == 0) log.info { "Elements processed: $index" } }
        .chunked(numWorkers)
        .forEach { chunk ->
            chunk.map {
                launch {
                    action(it)
                }
            }.joinAll()
        }
}
