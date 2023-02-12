package ru.itmo.stand.service.impl.neighbours.indexing

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.lucene.LuceneDocument
import ru.itmo.stand.util.toDoubleArray
import smile.clustering.XMeans
import java.util.concurrent.atomic.AtomicInteger

@Service
class VectorIndexBuilder(
    private val embeddingCalculator: BertEmbeddingCalculator
) {



    fun indexDocuments(documents: Sequence<Pair<String, List<LuceneDocument>>>): Int {

        val sem = Semaphore(MAX_CONCURRENCY)

        val counter = AtomicInteger(0)
        val clusterSizes = AtomicInteger(0)

        runBlocking {
            documents.forEach {
                launch {
                    sem.withPermit {
                        val k = process(it)
                        clusterSizes.addAndGet(k)
                        counter.incrementAndGet()
                    }
                }
            }
        }
        return (clusterSizes.get() / counter.get())
    }

    private fun process(token :Pair<String, List<LuceneDocument>>): Int {
        val embeddings = embeddingCalculator.calculate(
            token.second.map { it.content }.toTypedArray()
        )

        val clusterModel = XMeans.fit(embeddings.toDoubleArray(), 16)

        val centroids = clusterModel.centroids;

        return clusterModel.k;
    }

    companion object {
        const val MAX_CONCURRENCY = 10;
    }

}