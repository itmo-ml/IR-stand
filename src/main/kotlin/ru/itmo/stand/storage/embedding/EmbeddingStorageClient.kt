package ru.itmo.stand.storage.embedding

import io.weaviate.client.WeaviateClient
import io.weaviate.client.base.Result
import io.weaviate.client.v1.batch.model.ObjectGetResponse
import io.weaviate.client.v1.data.model.WeaviateObject
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel
import io.weaviate.client.v1.graphql.model.GraphQLResponse
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument
import io.weaviate.client.v1.graphql.query.fields.Field
import io.weaviate.client.v1.misc.model.VectorIndexConfig
import io.weaviate.client.v1.schema.model.Property
import io.weaviate.client.v1.schema.model.Schema
import io.weaviate.client.v1.schema.model.WeaviateClass
import org.springframework.stereotype.Service
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding

@Service
class EmbeddingStorageClient(
    private val client: WeaviateClient,
) {

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

    fun findByVector(vector: Array<Float>): Result<GraphQLResponse> = client.graphQL()
        .get()
        .withClassName(className)
        .withFields(tokenField, docIdField, additionalField)
        .withNearVector(NearVectorArgument.builder().vector(vector).build())
        .run()

    fun findSchema(): Result<Schema> = client.schema()
        .getter()
        .run()

    fun deleteAllModels(): Result<Boolean> = client.schema()
        .allDeleter()
        .run()

    fun index(embedding: ContextualizedEmbedding): Result<Array<ObjectGetResponse>> {
        val obj = WeaviateObject.builder()
            .vector(embedding.embedding)
            .properties(
                mapOf(
                    ContextualizedEmbedding::token.name to embedding.token,
                    ContextualizedEmbedding::embeddingId.name to embedding.embeddingId,
                ),
            )
            .className(className)
            .build()

        return client.batch().objectsBatcher()
            .withObjects(obj)
            .withConsistencyLevel(ConsistencyLevel.ONE)
            .run()
    }

    fun indexBatch(embeddings: List<ContextualizedEmbedding>): Result<Array<ObjectGetResponse>> {
        val objects = embeddings.map {
            WeaviateObject.builder()
                .vector(it.embedding)
                .properties(
                    mapOf(
                        ContextualizedEmbedding::token.name to it.token,
                        ContextualizedEmbedding::embeddingId.name to it.embeddingId
                    )
                )
                .className(className)
                .build()
        }.toTypedArray()

        return client.batch().objectsBatcher()
            .withObjects(*objects)
            .withConsistencyLevel(ConsistencyLevel.ONE)
            .run()
    }


    fun ensureContextualizedEmbeddingModel(): Result<WeaviateClass> {
        val foundClass = client.schema().classGetter()
            .withClassName(className)
            .run()
        if (foundClass.result != null) return foundClass

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
            return client.schema().classGetter()
                .withClassName(className)
                .run()
        }

        error("Failed to ensure class [$weaviateClass]. Error: ${createdClass.error}")
    }
}
