package ru.itmo.stand.storage.embedding.neighbours.hnsw

import com.github.jelmerk.knn.DistanceFunctions
import com.github.jelmerk.knn.JavaObjectSerializer
import com.github.jelmerk.knn.hnsw.HnswIndex
import io.github.oshai.KotlinLogging
import jakarta.annotation.PreDestroy
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.embedding.neighbours.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.neighbours.model.ContextualizedEmbedding
import ru.itmo.stand.util.createPath
import java.io.File

class ContextualizedEmbeddingInMemoryRepository(
    private val standProperties: StandProperties,
) : ContextualizedEmbeddingRepository {

    private val log = KotlinLogging.logger { }
    private val itemIdSerializer = JavaObjectSerializer<String>()
    private val itemSerializer = JavaObjectSerializer<ContextualizedEmbedding>()
    private val indexFile = File("${standProperties.app.basePath}/indexes/neighbours/hnsw").createPath()

    private var index = runCatching {
        HnswIndex.load<String, FloatArray, ContextualizedEmbedding, Float>(indexFile)
    }.getOrElse {
        log.info {
            "Got exception [${it.javaClass.simpleName}] during index loading with message: ${it.message}. " +
                "Cause: ${it.cause?.message}"
        }
        HnswIndex.newBuilder(
            standProperties.app.neighboursAlgorithm.bertModelType.dimensions,
            DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE,
            64_000_000,
        ).withCustomSerializers(itemIdSerializer, itemSerializer)
            // TODO: configure these values
            .withEf(64)
            .withM(64)
            .withEfConstruction(128)
            .build()
    }

    @PreDestroy
    fun saveIndex() {
        index.save(indexFile)
    }

    override fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding> =
        index.findNearest(vector.toFloatArray(), 10) // TODO: configure this value
            .filter { it.distance() <= 4 } // TODO: configure this value
            .map { it.item() }

    override fun index(embedding: ContextualizedEmbedding) {
        index.add(embedding)
    }

    override fun indexBatch(embeddings: List<ContextualizedEmbedding>) {
        index.addAll(embeddings)
    }
}
