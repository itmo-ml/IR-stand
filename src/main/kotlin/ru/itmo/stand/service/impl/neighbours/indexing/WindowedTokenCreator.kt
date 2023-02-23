package ru.itmo.stand.service.impl.neighbours.indexing

import edu.stanford.nlp.naturalli.ClauseSplitter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.insert
import org.springframework.stereotype.Service
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.lucene.LuceneDocument
import ru.itmo.stand.service.lucene.LuceneService
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.service.sqlite.SqliteDocument
import ru.itmo.stand.service.sqlite.SqliteService
import ru.itmo.stand.util.processParallel
import java.util.concurrent.atomic.AtomicInteger

@Service
class WindowedTokenCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    //private val luceneService: LuceneService,
    private val sqliteService: SqliteService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(documents: Sequence<Document>) {
/*        processParallel(documents, MAX_CONCURRENCY, log) {
            create(it)
        }*/

        for((index, doc) in documents.withIndex()) {
            if(index % 100 == 0) log.info("Documents processed: {}", index)
            create(doc)
        }

        sqliteService.completeIndexing()
        //luceneService.completeIndexing()
    }

    fun create(document: Document) {
        val windows = preprocessingPipelineExecutor.execute(document.content)
        val windowedTokens = windows.map {
            SqliteDocument(
                groupKey = it.middleToken,
                documentId = document.id,
                content = it.convertContentToString(),
            )
        }
        sqliteService.saveInBatch(windowedTokens)
    }

    companion object {
        const val MAX_CONCURRENCY = 10;
    }
}
