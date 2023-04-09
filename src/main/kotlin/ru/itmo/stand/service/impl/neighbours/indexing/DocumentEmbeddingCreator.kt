package ru.itmo.stand.service.impl.neighbours.indexing

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.storage.lucene.model.NeighboursEmbedding
import ru.itmo.stand.storage.lucene.repository.NeighboursEmbeddingRepository

@Service
class DocumentEmbeddingCreator(
    private val neighboursEmbeddingRepository: NeighboursEmbeddingRepository,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(documents: Sequence<Document>) {
        documents.onEachIndexed { index, _ -> if (index % 10000 == 0) log.info("Document embeddings created: {}", index) }
            .chunked(BERT_BATCH_SIZE)
            .forEach { chunk ->
                val embeddings = bertEmbeddingCalculator.calculate(chunk.map { it.content }.toTypedArray())
                    .mapIndexed { index, embedding -> NeighboursEmbedding(chunk[index].id, embedding) }
                neighboursEmbeddingRepository.saveAll(embeddings)
            }
        neighboursEmbeddingRepository.completeIndexing()
    }

    companion object {
        const val BERT_BATCH_SIZE = 100
    }
}
