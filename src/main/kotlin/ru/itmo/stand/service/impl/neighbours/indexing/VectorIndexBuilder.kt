package ru.itmo.stand.service.impl.neighbours.indexing

import io.github.oshai.KotlinLogging
import kotlinx.coroutines.flow.asFlow
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator.Companion.TOKEN_WINDOWS_SEPARATOR
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator.Companion.WINDOWS_SEPARATOR
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator.Companion.WINDOW_DOC_IDS_SEPARATOR
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding.Companion.TOKEN_AND_EMBEDDING_ID_SEPARATOR
import ru.itmo.stand.util.processConcurrently
import ru.itmo.stand.util.toDoubleArray
import ru.itmo.stand.util.toFloatArray
import smile.clustering.XMeans
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@Service
class VectorIndexBuilder(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val embeddingCalculator: BertEmbeddingCalculator,
) {

    private val log = KotlinLogging.logger { }

    suspend fun index(windowedTokensFile: File) {
        log.info { "Starting vector indexing" }
        val windowsByTokenPairs = readWindowsByTokenPairs(windowedTokensFile)

        val counter = AtomicInteger(0)
        val clusterSizes = AtomicInteger(0)
        val windowsCount = AtomicInteger(0)

        processConcurrently(
            windowsByTokenPairs.asFlow(),
            MAX_CONCURRENCY,
            { if (it % 10 == 0) log.info { "Elements processed: $it" } },
        ) {
            windowsCount.addAndGet(it.second.size)
            val k = process(it)
            clusterSizes.addAndGet(k)
            counter.incrementAndGet()
        }

        log.info { "Token count: ${counter.get()}" }
        log.info { "Cluster sizes: ${clusterSizes.get()}" }
        log.info { "Windows count: ${windowsCount.get()}" }
        log.info { "Mean windows per token: ${windowsCount.get().toDouble() / counter.get().toDouble()}" }
        log.info { "Mean cluster size is ${clusterSizes.get() / counter.get().toFloat()}" }
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
            token to windows.map { it.split(WINDOW_DOC_IDS_SEPARATOR).first() }
        }

    fun process(token: Pair<String, Collection<String>>): Int {
        val embeddings = embeddingCalculator.calculate(token.second, BERT_BATCH_SIZE)

        val doubleEmb = embeddings.toDoubleArray()

        val clusterModel = XMeans.fit(doubleEmb, 8) // TODO: configure this value

        log.info { "${token.first} got ${clusterModel.k} centroids" }

        val centroids = clusterModel.centroids

        val contextualizedEmbeddings = centroids.map { it.toFloatArray() }.mapIndexed { index, centroid ->
            ContextualizedEmbedding(
                tokenWithEmbeddingId = "${token.first}$TOKEN_AND_EMBEDDING_ID_SEPARATOR$index",
                embedding = centroid.toFloatArray(),
            )
        }
        contextualizedEmbeddingRepository.indexBatch(contextualizedEmbeddings)

        return clusterModel.k
    }

    companion object {
        const val MAX_CONCURRENCY = 10
        const val BERT_BATCH_SIZE = 10000
    }
}
