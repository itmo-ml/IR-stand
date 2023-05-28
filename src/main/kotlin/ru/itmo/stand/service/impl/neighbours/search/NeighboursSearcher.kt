package ru.itmo.stand.service.impl.neighbours.search

import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.TokensPipelineExecutor
import ru.itmo.stand.service.impl.neighbours.WindowsPipelineExecutor
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.lucene.repository.neighbours.InvertedIndex

@Service
class NeighboursSearcher(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val windowsPipelineExecutor: WindowsPipelineExecutor,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
    private val invertedIndex: InvertedIndex,
    private val tokensPipelineExecutor: TokensPipelineExecutor
) {

    fun search(query: String): List<String> {
        val tokens = tokensPipelineExecutor.execute(query)

        val primaryDocuments = invertedIndex.findByTokens(tokens)

        val windows = windowsPipelineExecutor.execute(query)
        val embeddings = bertEmbeddingCalculator.calculate(windows.map { it.toTranslatorInput() }.toTypedArray())

        return embeddings.flatMap { embedding -> contextualizedEmbeddingRepository.findByVector(embedding.toTypedArray()) }
            .let { contextualizedEmbeddings ->
                val tokenWithEmbeddingIds = contextualizedEmbeddings.map { it.tokenWithEmbeddingId }
                val secondaryDocuments = invertedIndex.findByTokenWithEmbeddingIds(tokenWithEmbeddingIds)

                sequenceOf(primaryDocuments, secondaryDocuments).flatten()
                    .groupingBy { it.docId }
                    .foldTo(HashMap(), 0f) { acc, doc -> acc + doc.score }

            }.entries
            .sortedByDescending { (_, score) -> score }
            .take(10) // TODO: configure this value
            .map { (docId, _) -> docId }
    }
}
