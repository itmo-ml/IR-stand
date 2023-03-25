package ru.itmo.stand.storage.vector

import io.weaviate.client.Config
import io.weaviate.client.WeaviateClient
import io.weaviate.client.v1.data.model.WeaviateObject
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument
import io.weaviate.client.v1.graphql.query.fields.Field
import io.weaviate.client.v1.misc.model.VectorIndexConfig
import io.weaviate.client.v1.schema.model.Property
import io.weaviate.client.v1.schema.model.WeaviateClass

class WeaviateRepository {

    fun test() {
        val config = Config("http", "localhost:8080")
        val client = WeaviateClient(config)

        val classParam = WeaviateClass.builder()
            .className("context_vectors")
            .vectorIndexType("hnsw")
            .vectorIndexConfig(
                VectorIndexConfig.builder()
                    .build(),
            )
            .properties(
                listOf(
                    Property.builder()
                        .name("embedding_id")
                        .dataType(listOf("string"))
                        .build(),
                    Property.builder()
                        .name("token")
                        .dataType(listOf("string"))
                        .build(),
                ),
            )
            .build()

//    var classResult = client.schema().classCreator()
//        .withClass(classParam)
//        .run()

        val vector = floatArrayOf(1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f).toTypedArray()

        val obj = WeaviateObject.builder()
            .vector(vector)
            .properties(
                mapOf(
                    "embedding_id" to "test",
                    "token" to "testtoken",
                ),
            )
            .className("context_vectors")
            .build()

        val batchResult = client.batch().objectsBatcher()
            .withObject(obj)
            .withConsistencyLevel(ConsistencyLevel.ONE)
            .run()

        val additional = Field.builder()
            .name("_additional")
            .fields(
                arrayOf(
                    Field.builder().name("vector").build(),
                ),
            ).build()

        val nearResults = client.graphQL()
            .get()
            .withClassName("Context_vectors")
            .withFields(Field.builder().name("token").build(), Field.builder().name("embedding_id").build(), additional)
            .withNearVector(NearVectorArgument.builder().vector(vector).build())
            .run()
    }
}
