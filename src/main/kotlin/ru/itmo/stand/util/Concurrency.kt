package ru.itmo.stand.util

import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

suspend fun <T> processConcurrently(
    data: Flow<T>,
    numWorkers: Int,
    progressListener: (Int) -> Unit,
    action: suspend (T) -> Unit,
): Unit = coroutineScope {
    val index = AtomicInteger(0)
    val semaphore = Semaphore(numWorkers)
    data.onEach { progressListener(index.getAndIncrement()) }
        .map { datum -> launch { semaphore.withPermit { action(datum) } } }
        .buffer(capacity = UNLIMITED)
        .collect { it.join() }
}
