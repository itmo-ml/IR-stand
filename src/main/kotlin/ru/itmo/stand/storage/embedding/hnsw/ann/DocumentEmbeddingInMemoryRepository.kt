package ru.itmo.stand.storage.embedding.hnsw.ann

import com.github.jelmerk.knn.DistanceFunctions
import com.github.jelmerk.knn.JavaObjectSerializer
import com.github.jelmerk.knn.hnsw.HnswIndex
import io.github.oshai.KotlinLogging
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.embedding.model.ann.DocumentEmbedding
import java.nio.file.Paths

@Repository
class DocumentEmbeddingInMemoryRepository(
    private val standProperties: StandProperties,
) {

    private val log = KotlinLogging.logger { }
    private val itemIdSerializer = JavaObjectSerializer<String>()
    private val itemSerializer = JavaObjectSerializer<DocumentEmbedding>()

    private var index = runCatching {
        val indexPath = Paths.get("${standProperties.app.basePath}/indexes/ann/hnsw")
        HnswIndex.load<String, FloatArray, DocumentEmbedding, Float>(indexPath)
    }.getOrElse {
        log.info { "Got exception [${it.javaClass.simpleName}] during index loading with message: ${it.message}" }
        HnswIndex.newBuilder(
            standProperties.app.annAlgorithm.bertModelType.dimensions,
            DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE,
            64_000_000,
        ).withCustomSerializers(itemIdSerializer, itemSerializer)
            // defaults for weaviate
            .withEf(64)
            .withM(64)
            .withEfConstruction(128)
            .build()
    }

    @PreDestroy
    fun saveIndex() {
        index.save(Paths.get("${standProperties.app.basePath}/indexes/ann/hnsw"))
    }

    fun findByVector(vector: Array<Float>, topN: Int): List<DocumentEmbedding> =
        index.findNearest(vector.toFloatArray(), topN).map { it.item() }

    fun index(embedding: DocumentEmbedding) {
        index.add(embedding)
    }

    fun indexBatch(embeddings: List<DocumentEmbedding>) {
        index.addAll(embeddings)
    }
}
