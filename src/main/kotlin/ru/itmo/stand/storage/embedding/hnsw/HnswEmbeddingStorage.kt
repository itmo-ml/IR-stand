package ru.itmo.stand.storage.embedding.hnsw

import com.github.jelmerk.knn.DistanceFunctions
import com.github.jelmerk.knn.JavaObjectSerializer
import com.github.jelmerk.knn.hnsw.HnswIndex
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.embedding.IEmbeddingStorage
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import java.nio.file.Paths

@Service
@ConditionalOnProperty(value = ["stand.app.neighbours-algorithm.embedding-storage"], havingValue = "HNSW")
class HnswEmbeddingStorage(
    private val standProperties: StandProperties,

) : IEmbeddingStorage {

    private val itemIdSerializer = JavaObjectSerializer<Int>()
    private val itemSerializer = JavaObjectSerializer<ContextualizedEmbedding>()

    private var index: HnswIndex<Int, FloatArray, ContextualizedEmbedding, Float> =
        // TODO move dimensions to config
        HnswIndex.newBuilder(128, DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE, 64_000_000)
            // defaults for weaviate
            .withCustomSerializers(itemIdSerializer, itemSerializer)
            .withEf(64)
            .withM(64)
            .withEfConstruction(128)
            .build()
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

    override fun loadIndex() {
        index = HnswIndex.load(Paths.get("${standProperties.app.basePath}/indexes/neighbours/hnsw"))
    }

    override fun saveIndex() {
        index.save(Paths.get("${standProperties.app.basePath}/indexes/neighbours/hnsw"))
    }
}
