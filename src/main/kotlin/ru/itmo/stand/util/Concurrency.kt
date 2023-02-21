package ru.itmo.stand.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.lucene.codecs.StoredFieldsFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun <T> processParallel(data: Sequence<T>, numWorkers: Int, action: (T) -> Unit) = runBlocking(Dispatchers.Default) {
    data
        .chunked(numWorkers)
        .mapIndexed { index, chunk ->
            chunk.map {
                launch {
                    //log.info("Elements processed: {}", index)
                    //if (index % 10 == 0) log.info("Elements processed: {}", index)
                    action(it)
                }
            }
        }
        .forEach { it.joinAll() }
}

fun <T> processParallelQueue(data: Sequence<T>, numWorkers: Int, log: Logger, action: (T) -> Unit) = runBlocking(Dispatchers.Default) {

    val channel = Channel<T>(numWorkers)

    repeat(numWorkers) {
        launch {
            for (doc in channel) {
                action(doc)
            }
        }
    }
    for((index, document) in data.withIndex()) {
        if(index % 100 == 0) log.info("elements processed: {}", index)
        channel.send(document)
    }
    channel.close()
}