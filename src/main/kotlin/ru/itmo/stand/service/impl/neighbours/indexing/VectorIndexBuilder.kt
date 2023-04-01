package ru.itmo.stand.service.impl.neighbours.indexing

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator.Companion.TOKEN_WINDOWS_SEPARATOR
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator.Companion.WINDOWS_SEPARATOR
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator.Companion.WINDOW_DOC_IDS_SEPARATOR
import ru.itmo.stand.util.kmeans.XMeans
import ru.itmo.stand.util.processParallel
import ru.itmo.stand.util.toDoubleArray
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@Service
class VectorIndexBuilder(
    private val embeddingCalculator: BertEmbeddingCalculator,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun index(windowedTokensFile: File): Int {
        val windowsByTokenPairs = readWindowsByTokenPairs(windowedTokensFile)

        log.info("starting vector indexing")
        val counter = AtomicInteger(0)
        val clusterSizes = AtomicInteger(0)
        val windowsCount = AtomicInteger(0)

        processParallel(windowsByTokenPairs, MAX_CONCURRENCY, log) {
            windowsCount.addAndGet(it.second.size)
            val k = process(it)
            clusterSizes.addAndGet(k)
            counter.incrementAndGet()
        }

        log.info("token count: ${counter.get()}")
        log.info("cluster sizes: ${clusterSizes.get()}")
        log.info("windows count: ${windowsCount.get()}")
        log.info("mean windows per token: ${windowsCount.get().toDouble() / counter.get().toDouble()}")
        return (clusterSizes.get() / counter.get())
    }

    private fun readWindowsByTokenPairs(windowedTokensFile: File) = windowedTokensFile
        .bufferedReader()
        .lineSequence()
        .map { line ->
            val tokenAndWindows = line.split(TOKEN_WINDOWS_SEPARATOR)
            val token = tokenAndWindows[0]
            val windows = tokenAndWindows[1]
                .split(WINDOWS_SEPARATOR)
                .filter { it.isNotBlank() }
                .take(1000)
            token to windows.map { it.split(WINDOW_DOC_IDS_SEPARATOR).first() }
        }

    fun process(token: Pair<String, Collection<String>>): Int {
        val embeddings = token.second.chunked(BERT_BATCH_SIZE)
            .flatMap { embeddingCalculator.calculate(it.toTypedArray()).asIterable() }
            .toTypedArray()

        val clusterModel = XMeans.fit(embeddings, 8)

        log.info("{} got centroids {}", token.first, clusterModel.k)

        val centroids = clusterModel.centroids

        return clusterModel.k
    }

    companion object {
        const val MAX_CONCURRENCY = 10
        const val BERT_BATCH_SIZE = 10000
    }
}
