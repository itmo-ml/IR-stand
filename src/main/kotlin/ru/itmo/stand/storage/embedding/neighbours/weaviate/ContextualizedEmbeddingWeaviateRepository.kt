package ru.itmo.stand.storage.embedding.neighbours.weaviate

import edu.stanford.nlp.naturalli.ClauseSplitter.log
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
import jakarta.annotation.PostConstruct
import ru.itmo.stand.storage.embedding.neighbours.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.neighbours.model.ContextualizedEmbedding

class ContextualizedEmbeddingWeaviateRepository(
    private val client: WeaviateClient,
) : ContextualizedEmbeddingRepository {

    private val className = checkNotNull(ContextualizedEmbedding::class.simpleName)
    private val tokenField = Field.builder().name(ContextualizedEmbedding::tokenWithEmbeddingId.name).build()
    private val additionalField = Field.builder()
        .name("_additional")
        .fields(
            Field.builder().name("vector").build(),
            Field.builder().name("distance").build(),
            Field.builder().name("certainty").build(),
        )
        .build()

    @PostConstruct
    private fun setUp() {
        initialize()
        log.info("ContextualizedEmbeddingModel ensured")
    }

    override fun findByVector(vector: Array<Float>): List<ContextualizedEmbedding> {
        val result = client.graphQL()
            .get()
            .withClassName(className)
            .withFields(tokenField, additionalField)
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
                    tokenWithEmbeddingId = obj[ContextualizedEmbedding::tokenWithEmbeddingId.name] as String,
                    embedding = checkNotNull(additional["vector"]?.map { it.toFloat() }?.toTypedArray()?.toFloatArray()),
                )
            }
    }

    override fun index(embedding: ContextualizedEmbedding) {
        val obj = WeaviateObject.builder()
            .vector(embedding.embedding.toTypedArray())
            .properties(mapOf(ContextualizedEmbedding::tokenWithEmbeddingId.name to embedding.tokenWithEmbeddingId))
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
                .properties(mapOf(ContextualizedEmbedding::tokenWithEmbeddingId.name to it.tokenWithEmbeddingId))
                .className(className)
                .build()
        }.toTypedArray()

        client.batch().objectsBatcher()
            .withObjects(*objects)
            .withConsistencyLevel(ConsistencyLevel.ONE)
            .run()
    }

    fun findSchema(): Result<Schema> = client.schema()
        .getter()
        .run()

    fun deleteAllModels(): Boolean = client.schema()
        .allDeleter()
        .run()
        .result

    private fun initialize() {
        val foundClass = client.schema().classGetter()
            .withClassName(className)
            .run()
        if (foundClass.result != null) return

        val weaviateClass = WeaviateClass.builder()
            .className(className)
            .vectorIndexType("hnsw")
            .vectorIndexConfig(VectorIndexConfig.builder().build())
            .properties(
                listOf(
                    Property.builder()
                        .name(ContextualizedEmbedding::tokenWithEmbeddingId.name)
                        .dataType(listOf("string"))
                        .build(),
                ),
            )
            .build()

        val createdClass = client.schema().classCreator()
            .withClass(weaviateClass)
            .run()

        if (createdClass.result) return

        error("Failed to ensure class [$weaviateClass]. Error: ${createdClass.error}")
    }
}
