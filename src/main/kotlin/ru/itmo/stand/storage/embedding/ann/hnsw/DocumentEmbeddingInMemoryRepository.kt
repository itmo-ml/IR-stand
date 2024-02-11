package ru.itmo.stand.storage.embedding.ann.hnsw

import com.github.jelmerk.knn.DistanceFunctions
import com.github.jelmerk.knn.JavaObjectSerializer
import com.github.jelmerk.knn.hnsw.HnswIndex
import io.github.oshai.KotlinLogging
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.embedding.ann.model.DocumentEmbedding
import ru.itmo.stand.util.createPath
import java.io.File

@Repository
class DocumentEmbeddingInMemoryRepository(
    private val standProperties: StandProperties,
) {

    private val log = KotlinLogging.logger { }
    private val itemIdSerializer = JavaObjectSerializer<String>()
    private val itemSerializer = JavaObjectSerializer<DocumentEmbedding>()
    private val indexFile = File("${standProperties.app.basePath}/indexes/ann/hnsw").createPath()

    private val index = runCatching {
        HnswIndex.load<String, FloatArray, DocumentEmbedding, Float>(indexFile)
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
    private val exactIndex = index.asExactIndex()

    @PreDestroy
    fun saveIndex() {
        index.save(indexFile)
    }

    fun findByVector(vector: Array<Float>, topN: Int): List<DocumentEmbedding> =
        index.findNearest(vector.toFloatArray(), topN).map { it.item() }

    fun findExactByVector(vector: Array<Float>, topN: Int): List<DocumentEmbedding> =
        exactIndex.findNearest(vector.toFloatArray(), topN).map { it.item() }

    fun index(embedding: DocumentEmbedding) {
        index.add(embedding)
    }

    fun indexBatch(embeddings: List<DocumentEmbedding>) {
        index.addAll(embeddings)
    }
}
