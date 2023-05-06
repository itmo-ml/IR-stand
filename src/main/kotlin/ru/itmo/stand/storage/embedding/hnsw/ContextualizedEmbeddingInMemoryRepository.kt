package ru.itmo.stand.storage.embedding.hnsw

import com.github.jelmerk.knn.DistanceFunctions
import com.github.jelmerk.knn.JavaObjectSerializer
import com.github.jelmerk.knn.hnsw.HnswIndex
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import java.nio.file.Paths

class ContextualizedEmbeddingInMemoryRepository(
    private val standProperties: StandProperties,
) : ContextualizedEmbeddingRepository {

    private val log = LoggerFactory.getLogger(javaClass)
    private val itemIdSerializer = JavaObjectSerializer<String>()
    private val itemSerializer = JavaObjectSerializer<ContextualizedEmbedding>()

    private var index = runCatching {
        val indexPath = Paths.get("${standProperties.app.basePath}/indexes/neighbours/hnsw")
        HnswIndex.load<String, FloatArray, ContextualizedEmbedding, Float>(indexPath)
    }.getOrElse {
        log.info("Got exception [{}] during index loading with message: {}", it.javaClass.simpleName, it.message)
        // TODO move dimensions to config
        HnswIndex.newBuilder(128, DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE, 64_000_000)
            // defaults for weaviate
            .withCustomSerializers(itemIdSerializer, itemSerializer)
            .withEf(64)
            .withM(64)
            .withEfConstruction(128)
            .build()
    }

    @PreDestroy
    fun saveIndex() {
        index.save(Paths.get("${standProperties.app.basePath}/indexes/neighbours/hnsw"))
    }

    override fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding> {
        return index.findNearest(vector.toFloatArray(), 100)
            .map {
                it.item()
                // TODO distance is already calculated here, not need to recalculate it in index builder
                // it.distance()
            }
    }

    override fun deleteAllModels(): Boolean {
        TODO("No such api(")
    }

    override fun index(embedding: ContextualizedEmbedding) {
        index.add(embedding)
    }

    override fun indexBatch(embeddings: List<ContextualizedEmbedding>) {
        index.addAll(embeddings)
    }

    override fun initialize(): Boolean {
        return true
    }
}
