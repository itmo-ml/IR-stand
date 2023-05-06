package ru.itmo.stand.service.impl.neighbours.search

import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.lucene.repository.neighbours.InvertedIndex

@Service
class NeighboursSearcher(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
    private val invertedIndex: InvertedIndex,
) {

    fun search(query: String): List<String> {
        val windows = preprocessingPipelineExecutor.execute(query)
        val embeddings = bertEmbeddingCalculator.calculate(windows.map { it.convertContentToString() }.toTypedArray())
        return embeddings.flatMap { embedding -> contextualizedEmbeddingRepository.findByVector(embedding.toTypedArray()) }
            .map { contextualizedEmbedding ->
                invertedIndex.findByTokenWithEmbeddingId(contextualizedEmbedding.tokenWithEmbeddingId)
                    .groupingBy { it.docId }
                    .foldTo(HashMap(), 0f) { acc, doc -> acc + doc.score }
            }.fold(HashMap<String, Float>()) { acc, scoreByDocIdMap ->
                scoreByDocIdMap.forEach { (docId, score) -> acc.merge(docId, score) { prev, new -> prev + new } }
                acc
            }.entries
            .sortedByDescending { (_, score) -> score }
            .take(10) // TODO: configure this value
            .map { (docId, _) -> docId }
    }
}
