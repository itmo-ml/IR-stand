package ru.itmo.stand.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.lucene.codecs.StoredFieldsFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun <T> processParallel(data: Sequence<T>, numWorkers: Int, log: Logger,  action: (T) -> Unit) = runBlocking(Dispatchers.Default) {
    data
        .onEachIndexed { index, _ ->  if (index % 10 == 0) log.info("Elements processed: {}", index)}
        .chunked(numWorkers)
        .mapIndexed { index, chunk ->
            chunk.map {
                launch {
                    action(it)
                }
            }
        }
        .forEach { it.joinAll() }
}