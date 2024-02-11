package ru.itmo.stand.service.impl.neighbours.indexing

import io.github.oshai.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.bert.TranslatorInput
import ru.itmo.stand.storage.embedding.neighbours.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.neighbours.model.ContextualizedEmbedding
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursDocument
import ru.itmo.stand.storage.lucene.repository.neighbours.DocumentEmbeddingRepository
import ru.itmo.stand.storage.lucene.repository.neighbours.InvertedIndex
import ru.itmo.stand.util.cos
import ru.itmo.stand.util.processConcurrently
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Service
class InvertedIndexBuilder(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val documentEmbeddingRepository: DocumentEmbeddingRepository,
    private val embeddingCalculator: BertEmbeddingCalculator,
    private val standProperties: StandProperties,
    private val invertedIndex: InvertedIndex,
) {

    private val log = KotlinLogging.logger { }
    private val documentEmbeddingCache = ConcurrentHashMap<String, FloatArray>()

    fun index(windowedTokensFile: File) = runBlocking(Dispatchers.Default) {
        val tokensWithWindows = readTokensWindowsAndDocIds(windowedTokensFile)

        processConcurrently(
            tokensWithWindows.asFlow(),
            10,
            { log.info { "Tokens processed: $it" } },
        ) { tokenWithWindows ->
            val (_, docIdsByWindowPairs) = tokenWithWindows
            val (windows, docIdsList) = docIdsByWindowPairs.unzip()
            embeddingCalculator.calculate(windows, standProperties.app.neighboursAlgorithm.bertWindowBatchSize)
                .forEachIndexed { index, embedding ->
                    val docIds = docIdsList[index]
                    contextualizedEmbeddingRepository.findByVector(embedding.toTypedArray())
                        .forEach { computeScoreAndSave(docIds, it) }
                }
        }

        invertedIndex.completeIndexing()
    }

    private fun readTokensWindowsAndDocIds(windowedTokensFile: File) = windowedTokensFile
        .bufferedReader()
        .lineSequence()
        .map { line ->
            val (token, windowsString) = line.split(WindowedTokenCreator.TOKEN_WINDOWS_SEPARATOR)
            val docIdsByWindowPairs = windowsString
                .split(WindowedTokenCreator.WINDOWS_SEPARATOR)
                .filter { it.isNotBlank() }
                .map {
                    val (windowString, docIdsString) = it.split(WindowedTokenCreator.WINDOW_DOC_IDS_SEPARATOR)
                    val window = windowString.split(WindowedTokenCreator.WINDOW_TOKEN_INDEX_SEPARATOR)
                        .let { (tokenIndex, window) -> TranslatorInput(tokenIndex.toInt(), window) }
                    val docIds = docIdsString.split(WindowedTokenCreator.DOC_IDS_SEPARATOR)
                    window to docIds
                }
            TokenWindowsAndDocIds(token, docIdsByWindowPairs)
        }

    private fun computeScoreAndSave(
        docIds: List<String>,
        contextualizedEmbedding: ContextualizedEmbedding,
    ) {
        val neighboursDocuments = docIds.map { docId ->
            val documentEmbedding = documentEmbeddingCache.computeIfAbsent(docId) {
                documentEmbeddingRepository.findByDocId(docId).embedding
            }
            NeighboursDocument(
                tokenWithEmbeddingId = contextualizedEmbedding.tokenWithEmbeddingId,
                docId = docId,
                score = documentEmbedding.cos(contextualizedEmbedding.embedding),
            )
        }
        invertedIndex.saveAll(neighboursDocuments)
    }

    private data class TokenWindowsAndDocIds(
        val token: String,
        val docIdsByWindowPairs: List<Pair<TranslatorInput, List<String>>>,
    )
}
