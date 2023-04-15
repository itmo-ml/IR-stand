package ru.itmo.stand.storage.embedding.weaviate

import io.weaviate.client.WeaviateClient
import io.weaviate.client.base.Result
import io.weaviate.client.v1.data.model.WeaviateObject
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument
import io.weaviate.client.v1.graphql.query.fields.Field
import io.weaviate.client.v1.misc.model.VectorIndexConfig
import io.weaviate.client.v1.schema.model.Property
import io.weaviate.client.v1.schema.model.Schema
import io.weaviate.client.v1.schema.model.WeaviateClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.itmo.stand.storage.embedding.IEmbeddingStorage
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding

@Service
@ConditionalOnProperty(value= ["stand.app.neighbours-algorithm.embedding-storage"],havingValue = "WEAVIATE")
class WeaviateStorageClient(
    private val client: WeaviateClient,
): IEmbeddingStorage {

    private val className = checkNotNull(ContextualizedEmbedding::class.simpleName)
    private val tokenField = Field.builder().name(ContextualizedEmbedding::token.name).build()
    private val docIdField = Field.builder().name(ContextualizedEmbedding::embeddingId.name).build()
    private val additionalField = Field.builder()
        .name("_additional")
        .fields(
            Field.builder().name("vector").build(),
            Field.builder().name("distance").build(),
            Field.builder().name("certainty").build(),
        )
        .build()

    override fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding> {
        val result = client.graphQL()
            .get()
            .withClassName(className)
            .withFields(tokenField, docIdField, additionalField)
            .withNearVector(NearVectorArgument.builder().vector(vector).certainty(0.95f).build()) // TODO: configure this value
            .withLimit(10) // TODO: configure this value
            .run()
        check(result.error == null) { "Weaviate error: ${result.error}" }
        check(result.result.errors == null) { "GraphQL errors: ${result.result.errors}" }
        @Suppress("UNCHECKED_CAST")
        return checkNotNull((result.result.data as Map<String, Map<String, List<Map<String, *>>>>)["Get"]?.get("ContextualizedEmbedding"))
            .map { obj ->
                val additional = obj["_additional"] as Map<String, List<Double>>
                ContextualizedEmbedding(
                    token = obj["token"] as String,
                    embeddingId = (obj["embeddingId"] as Double).toInt(),
                    embedding = checkNotNull(additional["vector"]?.map { it.toFloat() }?.toTypedArray()?.toFloatArray()),
                )
            }
    }

    fun findSchema(): Result<Schema> = client.schema()
        .getter()
        .run()

    override fun deleteAllModels(): Boolean = client.schema()
        .allDeleter()
        .run()
        .result

    override fun index(embedding: ContextualizedEmbedding){
        val obj = WeaviateObject.builder()
            .vector(embedding.embedding.toTypedArray())
            .properties(
                mapOf(
                    ContextualizedEmbedding::token.name to embedding.token,
                    ContextualizedEmbedding::embeddingId.name to embedding.embeddingId,
                ),
            )
            .className(className)
            .build()

        client.batch().objectsBatcher()
            .withObjects(obj)
            .withConsistencyLevel(ConsistencyLevel.ONE)
            .run()
    }

    override fun indexBatch(embeddings: List<ContextualizedEmbedding>) {
        val objects = embeddings.map {
            WeaviateObject.builder()
                .vector(it.embedding.toTypedArray())
                .properties(
                    mapOf(
                        ContextualizedEmbedding::token.name to it.token,
                        ContextualizedEmbedding::embeddingId.name to it.embeddingId,
                    ),
                )
                .className(className)
                .build()
        }.toTypedArray()

        client.batch().objectsBatcher()
            .withObjects(*objects)
            .withConsistencyLevel(ConsistencyLevel.ONE)
            .run()
    }

    override fun initialize(): Boolean {
        val foundClass = client.schema().classGetter()
            .withClassName(className)
            .run()
        if (foundClass.result != null) return true

        val weaviateClass = WeaviateClass.builder()
            .className(className)
            .vectorIndexType("hnsw")
            .vectorIndexConfig(VectorIndexConfig.builder().build())
            .properties(
                listOf(
                    Property.builder()
                        .name(ContextualizedEmbedding::embeddingId.name)
                        .dataType(listOf("int"))
                        .build(),
                    Property.builder()
                        .name(ContextualizedEmbedding::token.name)
                        .dataType(listOf("string"))
                        .build(),
                ),
            )
            .build()

        val createdClass = client.schema().classCreator()
            .withClass(weaviateClass)
            .run()

        if (createdClass.result) {
            return true
        }

        error("Failed to ensure class [$weaviateClass]. Error: ${createdClass.error}")
    }

    override fun loadIndex() {
    }

    override fun saveIndex() {
    }
}
