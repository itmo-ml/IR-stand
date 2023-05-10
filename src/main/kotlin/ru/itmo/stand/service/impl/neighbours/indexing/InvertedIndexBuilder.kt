package ru.itmo.stand.service.impl.neighbours.indexing

import io.github.oshai.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.bert.TranslatorInput
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursDocument
import ru.itmo.stand.storage.lucene.repository.neighbours.DocumentEmbeddingRepository
import ru.itmo.stand.storage.lucene.repository.neighbours.InvertedIndex
import ru.itmo.stand.util.dot
import java.io.File

@Service
class InvertedIndexBuilder(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val documentEmbeddingRepository: DocumentEmbeddingRepository,
    private val embeddingCalculator: BertEmbeddingCalculator,
    private val invertedIndex: InvertedIndex,
) {

    private val log = KotlinLogging.logger { }
    private val documentEmbeddingCache = HashMap<String, FloatArray>()

    fun index(windowedTokensFile: File) {
        val tokensWithWindows = readTokensWithWindows(windowedTokensFile)

        tokensWithWindows.onEachIndexed { index, token ->
            log.info { "Tokens processed: $index. Current token: ${token.token}. Windows size: ${token.docIdsByWindowPairs.size}" }
        }.forEach { tokenWithWindows ->
            val (_, docIdsByWindowPairs) = tokenWithWindows
            val (windows, docIdsList) = docIdsByWindowPairs.unzip()
            embeddingCalculator.calculate(windows.map {
                TranslatorInput(it.first, it.second)
            }, BERT_BATCH_SIZE).forEachIndexed { index, embedding ->
                val docIds = docIdsList[index]
                contextualizedEmbeddingRepository.findByVector(embedding.toTypedArray())
                    .forEach { computeScoreAndSave(docIds, it) }
            }
        }

        invertedIndex.completeIndexing()
    }

    private fun readTokensWithWindows(windowedTokensFile: File) = windowedTokensFile
        .bufferedReader()
        .lineSequence()
        .map { line ->
            val (token, windowsString) = line.split(WindowedTokenCreator.TOKEN_WINDOWS_SEPARATOR)
            val docIdsByWindowPairs = windowsString
                .split(WindowedTokenCreator.WINDOWS_SEPARATOR)
                .filter { it.isNotBlank() }
                .map {
                    val (windowWithId, docIdsString) = it.split(WindowedTokenCreator.WINDOW_DOC_IDS_SEPARATOR)
                    val (window, tokenIndex) = windowWithId.split(WindowedTokenCreator.TOKEN_INDEX_SEPARATOR)

                    val docIds = docIdsString.split(WindowedTokenCreator.DOC_IDS_SEPARATOR)
                    Pair(tokenIndex.toLong(), window) to docIds
                }
            TokenWithWindows(token, docIdsByWindowPairs)
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
                score = documentEmbedding.dot(contextualizedEmbedding.embedding),
            )
        }
        invertedIndex.saveAll(neighboursDocuments)
    }

    private data class TokenWithWindows(
        val token: String,
        val docIdsByWindowPairs: List<Pair<Pair<Long, String>, List<String>>>,
    )

    companion object {
        const val BERT_BATCH_SIZE = 10_000
    }
}
