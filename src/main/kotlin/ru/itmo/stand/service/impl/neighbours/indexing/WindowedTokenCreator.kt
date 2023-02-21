package ru.itmo.stand.service.impl.neighbours.indexing

import edu.stanford.nlp.naturalli.ClauseSplitter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.lucene.LuceneDocument
import ru.itmo.stand.service.lucene.LuceneService
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.util.processParallel
import java.util.concurrent.atomic.AtomicInteger

@Service
class WindowedTokenCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val luceneService: LuceneService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(documents: Sequence<Document>) {
        processParallel(documents, MAX_CONCURRENCY, log) {
            create(it)
        }

        luceneService.completeIndexing()
    }

    fun create(document: Document) {
        val windows = preprocessingPipelineExecutor.execute(document.content)
        val windowedTokens = windows.map {
            LuceneDocument(
                groupKey = it.middleToken,
                documentId = document.id,
                content = it.convertContentToString(),
            )
        }
        luceneService.saveInBatch(windowedTokens)
    }

    companion object {
        const val MAX_CONCURRENCY = 10;
    }
}
