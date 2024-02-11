package ru.itmo.stand.service.impl.neighbours.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.storage.embedding.neighbours.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursDocument
import ru.itmo.stand.storage.lucene.repository.neighbours.InvertedIndex
import java.util.stream.Collectors.groupingBy

@Service
class NeighboursSearcher(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
    private val invertedIndex: InvertedIndex,
) {

    /**
     * TODO:
     *  1. sort by doc id
     *  2. use filed selector to load only doc id and score
     *  3. try hit collector
     *  4. try cache field
     *  5. use filter instead of query
     *  see https://cwiki.apache.org/confluence/display/lucene/ImproveSearchingSpeed#
     */
    fun search(query: String): List<String> = runBlocking(Dispatchers.Default) {
        val windows = preprocessingPipelineExecutor.execute(query)
        val documents = mutableSetOf<NeighboursDocument>()
        windows.asFlow()
            .map { bertEmbeddingCalculator.calculate(it.toTranslatorInput()) }
            .buffer()
            .map { contextualizedEmbeddingRepository.findByVector(it.toTypedArray()) }
            .buffer()
            .collect { embs ->
                documents += invertedIndex.findByTokenWithEmbeddingIds(embs.map { it.tokenWithEmbeddingId })
            }
        documents
            .groupingBy { it.docId }
            .foldTo(HashMap(), 0f) { acc, doc -> acc + doc.score }
            .entries
            .sortedByDescending { (_, score) -> score }
            .take(10) // TODO: configure this value
            .map { (docId, _) -> docId }
//        val embeddings = bertEmbeddingCalculator.calculate(windows.map { it.toTranslatorInput() }.toTypedArray())
//        embeddings.flatMap { embedding -> contextualizedEmbeddingRepository.findByVector(embedding.toTypedArray()) }
//            .let { contextualizedEmbeddings ->
//                val tokenWithEmbeddingIds = contextualizedEmbeddings.map { it.tokenWithEmbeddingId }
//                invertedIndex.findByTokenWithEmbeddingIds(tokenWithEmbeddingIds)
//                    .toSet()
//                    .groupingBy { it.docId }
//                    .foldTo(HashMap(), 0f) { acc, doc -> acc + doc.score }
//            }.entries
//            .sortedByDescending { (_, score) -> score }
//            .take(10) // TODO: configure this value
//            .map { (docId, _) -> docId }
    }
}
