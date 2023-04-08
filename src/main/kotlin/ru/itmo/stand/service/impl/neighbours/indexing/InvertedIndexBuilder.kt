package ru.itmo.stand.service.impl.neighbours.indexing

import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.storage.embedding.EmbeddingStorageClient
import java.io.File

@Service
class InvertedIndexBuilder(
    private val embeddingStorageClient: EmbeddingStorageClient,
    private val embeddingCalculator: BertEmbeddingCalculator,
) {
    fun index(windowedTokensFile: File) {
        val tokens = windowedTokensFile
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

        tokens.forEach { tokenWithWindows ->
            val (token, docIdsByWindowPairs) = tokenWithWindows
            val (windows, docIdsList) = docIdsByWindowPairs.unzip()
            val embeddings = embeddingCalculator.calculate(windows.toTypedArray())
            embeddings.forEach { embedding ->
                embeddingStorageClient.findByVector(embedding.toTypedArray()).forEach {
                    TODO("Not yet implemented")
                }
            }
        }
    }

    data class TokenWithWindows(
        val token: String,
        val docIdsByWindowPairs: List<Pair<String, List<String>>>,
    )
}
