package ru.itmo.stand.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
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
    data.map { datum -> launch { semaphore.withPermit { action(datum) } } }
        /*
        In cases where one or more workers take longer than the combined time of the others,
        an increased buffer is utilized to prevent the emitter coroutine from stalling,
        allowing the remaining workers to continue their tasks while the first coroutine completes its execution.
        NOTE: Using an unlimited buffer results in unnecessary memory consumption for large amounts of data.
         */
        .buffer(numWorkers * 10)
        .collect {
            it.join()
            progressListener(index.incrementAndGet())
        }
}
