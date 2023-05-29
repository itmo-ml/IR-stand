package ru.itmo.stand.service.impl.neighbours.indexing

import io.github.oshai.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.bert.TranslatorInput
import ru.itmo.stand.service.impl.neighbours.TokensPipelineExecutor
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.storage.embedding.ContextualizedEmbeddingRepository
import ru.itmo.stand.storage.embedding.model.ContextualizedEmbedding
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursDocument
import ru.itmo.stand.storage.lucene.repository.neighbours.DocumentEmbeddingRepository
import ru.itmo.stand.storage.lucene.repository.neighbours.InvertedIndex
import ru.itmo.stand.util.dot
import java.io.File
import kotlin.math.ln

@Service
class InvertedIndexBuilder(
    private val contextualizedEmbeddingRepository: ContextualizedEmbeddingRepository,
    private val documentEmbeddingRepository: DocumentEmbeddingRepository,
    private val embeddingCalculator: BertEmbeddingCalculator,
    private val invertedIndex: InvertedIndex,
    private val tokensPipelineExecutor: TokensPipelineExecutor
) {

    private val log = KotlinLogging.logger { }
    private val documentEmbeddingCache = HashMap<String, FloatArray>()



    fun index(windowedTokensFile: File, documents: Sequence<Document>) {

        val (tf, idf) = getTfIdf(documents)

        val tokensWithWindows = readTokensWindowsAndDocIds(windowedTokensFile)

        tokensWithWindows.onEachIndexed { index, token ->
            log.info { "Tokens processed: $index. Current token: ${token.token}. Windows size: ${token.docIdsByWindowPairs.size}" }
        }.forEach { tokenWithWindows ->
            val (_, docIdsByWindowPairs) = tokenWithWindows
            val (windows, docIdsList) = docIdsByWindowPairs.unzip()
            embeddingCalculator.calculate(windows, BERT_BATCH_SIZE).forEachIndexed { index, embedding ->
                val docIds = docIdsList[index]
                contextualizedEmbeddingRepository.findByVector(embedding.toTypedArray())
                    .forEach { computeScoreAndSave(docIds, it, tf, idf) }
            }
        }

        invertedIndex.completeIndexing()
    }

    private fun getTfIdf(documents: Sequence<Document>): Pair<Map<String, Map<String, Double>>,Map<String, Double> > {

        val df = mutableMapOf<String, HashSet<String>>()
        val tf = mutableMapOf<String, MutableMap<String, Double>>()

        var docsCount = 0.0
        for(doc in documents){
            docsCount++
            val tokens = tokensPipelineExecutor.execute(doc.content)
            tf[doc.id] = mutableMapOf()

            tokens.forEach {
                df.putIfAbsent(it, hashSetOf())
                df[it]?.add(doc.id)

                tf[doc.id]?.putIfAbsent(it, 0.0)
                tf[doc.id]?.set(it, tf[doc.id]?.get(it)?.plus((1.0 / tokens.size)) ?: 0.0)
            }
        }

        val idf = df.map {
            it.key to ln(docsCount / it.value.size)
        }.toMap()

        return Pair(tf, idf)
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
        tf: Map<String, Map<String, Double>>,
        idf: Map<String, Double>,
    ) {
        val token = contextualizedEmbedding.tokenWithEmbeddingId.split(ContextualizedEmbedding.TOKEN_AND_EMBEDDING_ID_SEPARATOR).first()
        val neighboursDocuments = docIds.map { docId ->
            val documentEmbedding = documentEmbeddingCache.computeIfAbsent(docId) {
                documentEmbeddingRepository.findByDocId(docId).embedding
            }
            NeighboursDocument(
                token = token,
                tokenWithEmbeddingId = contextualizedEmbedding.tokenWithEmbeddingId,
                docId = docId,
                score = documentEmbedding.dot(contextualizedEmbedding.embedding) * (tf[docId]?.get(token)?.toFloat() ?: 1.0f) * (idf[token]?.toFloat() ?: 1.0f),
            )
        }
        invertedIndex.saveAll(neighboursDocuments)
    }

    private data class TokenWindowsAndDocIds(
        val token: String,
        val docIdsByWindowPairs: List<Pair<TranslatorInput, List<String>>>,
    )

    companion object {
        const val BERT_BATCH_SIZE = 10_000
    }
}
