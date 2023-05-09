package ru.itmo.stand.service.impl.neighbours.indexing

import io.github.oshai.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursEmbedding
import ru.itmo.stand.storage.lucene.repository.neighbours.DocumentEmbeddingRepository

@Service
class DocumentEmbeddingCreator(
    private val documentEmbeddingRepository: DocumentEmbeddingRepository,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
) {

    private val log = KotlinLogging.logger { }

    fun create(documents: Sequence<Document>) {
        documents.onEachIndexed { index, _ -> if (index % 10000 == 0) log.info { "Document embeddings created: $index" } }
            .chunked(BERT_BATCH_SIZE)
            .forEach { chunk ->
                val embeddings = bertEmbeddingCalculator.calculate(chunk.map { it.content }.toTypedArray())
                    .mapIndexed { index, embedding -> NeighboursEmbedding(chunk[index].id, embedding) }
                documentEmbeddingRepository.saveAll(embeddings)
            }
        documentEmbeddingRepository.completeIndexing()
    }

    companion object {
        const val BERT_BATCH_SIZE = 100
    }
}
