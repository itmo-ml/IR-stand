package ru.itmo.stand.service.impl

import io.github.oshai.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.bert.TranslatorInput
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.storage.embedding.ann.hnsw.DocumentEmbeddingInMemoryRepository
import ru.itmo.stand.storage.embedding.ann.model.DocumentEmbedding
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.writeAsFileInMrrFormat
import java.io.File

@Service
@ConditionalOnProperty(value = ["stand.app.method"], havingValue = "ann")
class DocumentAnnService(
    private val documentEmbeddingInMemoryRepository: DocumentEmbeddingInMemoryRepository,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
    private val standProperties: StandProperties,
) : DocumentService {

    private val log = KotlinLogging.logger { }

    override fun find(id: String): String? {
        TODO("Not yet implemented")
    }

    override fun search(queries: File, format: Format): List<String> = when (format) {
        Format.JUST_QUERY -> search(queries.readLines().single())
        Format.MS_MARCO -> {
            val outputPath = "${standProperties.app.basePath}/outputs/" +
                "${standProperties.app.method.name.lowercase()}/resultInMrrFormat.tsv"
            writeAsFileInMrrFormat(queries, outputPath) { query -> search(query) }
            listOf("See output in $outputPath")
        }
    }

    private fun search(query: String): List<String> {
        val queryVector = bertEmbeddingCalculator.calculate(TranslatorInput.withClsWordIndex(query)).toTypedArray()
        val results = documentEmbeddingInMemoryRepository.findExactByVector(queryVector, 10)
        return results.map { it.id }
    }

    override fun save(content: String, withId: Boolean): String {
        val (id, document) = extractId(content)
        val embedding = bertEmbeddingCalculator.calculate(TranslatorInput.withClsWordIndex(document))
        documentEmbeddingInMemoryRepository.index(DocumentEmbedding(id, embedding))
        return id
    }

    override suspend fun saveInBatch(contents: File, withId: Boolean): List<String> {
        contents.lineSequence()
            .map { extractId(it) }
            .onEachIndexed { index, _ -> if (index % 1000 == 0) log.info { "Documents indexed: $index" } }
            .chunked(BERT_BATCH_SIZE)
            .forEach { chunk ->
                val inputs = chunk.map { TranslatorInput.withClsWordIndex(it.content) }.toTypedArray()
                val embeddings = bertEmbeddingCalculator.calculate(inputs)
                    .mapIndexed { index, embedding -> DocumentEmbedding(chunk[index].id, embedding) }
                documentEmbeddingInMemoryRepository.indexBatch(embeddings)
            }
        return emptyList()
    }

    override fun getFootprint(): String {
        TODO("Not yet implemented")
    }

    companion object {
        const val BERT_BATCH_SIZE = 100
    }
}
