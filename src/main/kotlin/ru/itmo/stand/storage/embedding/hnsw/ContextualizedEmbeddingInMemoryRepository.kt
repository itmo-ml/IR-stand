package ru.itmo.stand.storage.embedding.hnsw

import com.github.jelmerk.knn.DistanceFunctions
import com.github.jelmerk.knn.JavaObjectSerializer
import com.github.jelmerk.knn.hnsw.HnswIndex
import io.github.oshai.KotlinLogging
import jakarta.annotation.PreDestroy
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import java.nio.file.Paths

class ContextualizedEmbeddingInMemoryRepository(
    private val standProperties: StandProperties,
) : ContextualizedEmbeddingRepository {

    private val log = KotlinLogging.logger { }
    private val itemIdSerializer = JavaObjectSerializer<String>()
    private val itemSerializer = JavaObjectSerializer<ContextualizedEmbedding>()

    private var index = runCatching {
        val indexPath = Paths.get("${standProperties.app.basePath}/indexes/neighbours/hnsw")
        HnswIndex.load<String, FloatArray, ContextualizedEmbedding, Float>(indexPath)
    }.getOrElse {
        log.info { "Got exception [${it.javaClass.simpleName}] during index loading with message: ${it.message}" }
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
        index.save(Paths.get("${standProperties.app.basePath}/indexes/neighbours/hnsw"))
    }

    override fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding> =
        index.findNearest(vector.toFloatArray(), 10) // TODO: configure this value
            .filter { it.distance() <= 5 } // TODO: configure this value
            .map { it.item() }

    override fun index(embedding: ContextualizedEmbedding) {
        index.add(embedding)
    }

    override fun indexBatch(embeddings: List<ContextualizedEmbedding>) {
        index.addAll(embeddings)
    }
}
