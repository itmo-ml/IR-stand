package ru.itmo.stand.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("processParallel")

fun <T> processParallel(data: Sequence<T>, numWorkers: Int, action: (T) -> Unit) = runBlocking(Dispatchers.Default) {
    data
        .onEachIndexed{index, _ -> if (index % 1 == 0) log.info("Elements processed: {}", index)}
        .chunked(numWorkers)
        .map {chunk -> chunk.map { launch { action(it) }}}
        .forEach { it.joinAll() }
}