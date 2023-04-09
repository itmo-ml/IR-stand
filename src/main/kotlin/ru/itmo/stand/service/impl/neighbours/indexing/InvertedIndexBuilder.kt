package ru.itmo.stand.service.impl.neighbours.indexing

import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.storage.embedding.EmbeddingStorageClient
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import ru.itmo.stand.storage.lucene.model.NeighboursDocument
import ru.itmo.stand.storage.lucene.repository.NeighboursDocumentRepository
import ru.itmo.stand.storage.lucene.repository.NeighboursEmbeddingRepository
import ru.itmo.stand.util.dot
import java.io.File

@Service
class InvertedIndexBuilder(
    private val neighboursEmbeddingRepository: NeighboursEmbeddingRepository,
    private val neighboursDocumentRepository: NeighboursDocumentRepository,
    private val embeddingStorageClient: EmbeddingStorageClient,
    private val embeddingCalculator: BertEmbeddingCalculator,
) {
    fun index(windowedTokensFile: File) {
        val tokensWithWindows = readTokensWithWindows(windowedTokensFile)

        tokensWithWindows.forEach { tokenWithWindows ->
            val (_, docIdsByWindowPairs) = tokenWithWindows
            val (windows, docIdsList) = docIdsByWindowPairs.unzip()
            embeddingCalculator.calculate(windows.toTypedArray()).forEachIndexed { index, embedding ->
                val docIds = docIdsList[index]
                embeddingStorageClient.findByVector(embedding.toTypedArray())
                    .forEach { computeScoreAndSave(docIds, it) }
            }
        }
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
                    val (window, docIdsString) = it.split(WindowedTokenCreator.WINDOW_DOC_IDS_SEPARATOR)
                    val docIds = docIdsString.split(WindowedTokenCreator.DOC_IDS_SEPARATOR)
                    window to docIds
                }
            TokenWithWindows(token, docIdsByWindowPairs)
        }

    private fun computeScoreAndSave(
        docIds: List<String>,
        contextualizedEmbedding: ContextualizedEmbedding,
    ) {
        val neighboursDocuments = docIds.map { docId ->
            val documentEmbedding = neighboursEmbeddingRepository.findByDocId(docId).embedding // TODO: cache?
            NeighboursDocument(
                tokenWithEmbeddingId = "${contextualizedEmbedding.token}:${contextualizedEmbedding.embeddingId}",
                docId = docId,
                score = documentEmbedding.dot(contextualizedEmbedding.embedding.toFloatArray()),
            )
        }
        neighboursDocumentRepository.saveAll(neighboursDocuments)
    }

    data class TokenWithWindows(
        val token: String,
        val docIdsByWindowPairs: List<Pair<String, List<String>>>,
    )
}
