package ru.itmo.stand.service.impl.neighbours.indexing

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.lucene.LuceneDocument
import ru.itmo.stand.util.processParallel
import ru.itmo.stand.util.toDoubleArray
import smile.clustering.XMeans
import java.util.concurrent.atomic.AtomicInteger

@Service
class VectorIndexBuilder(
    private val embeddingCalculator: BertEmbeddingCalculator
) {
    private val log = LoggerFactory.getLogger(javaClass)



    fun indexDocuments(documents: Sequence<Pair<String, List<LuceneDocument>>>): Int {

        log.info("starting vector indexing")
        val counter = AtomicInteger(0)
        val clusterSizes = AtomicInteger(0)
        val windowsCount = AtomicInteger(0)

        processParallel(documents, MAX_CONCURRENCY) {
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

    private fun process(token :Pair<String, List<LuceneDocument>>): Int {

        val embeddings = token.second.chunked(BERT_BATCH_SIZE)
            .flatMap {
                embeddingCalculator.calculate(it.map { it.content }.toTypedArray())
                    .asIterable()
            }.toTypedArray()
        
        val clusterModel = XMeans.fit(embeddings.toDoubleArray(), 16)
        val centroids = clusterModel.centroids;

        return clusterModel.k;
    }

    companion object {
        const val MAX_CONCURRENCY = 4;
        const val BERT_BATCH_SIZE = 10000;
    }

}