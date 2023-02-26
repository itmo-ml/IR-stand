package ru.itmo.stand.service.impl.neighbours.indexing

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.lucene.LuceneDocument
import ru.itmo.stand.service.lucene.LuceneService
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.util.Window

@Service
class WindowedTokenCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val luceneService: LuceneService,
) {

    private val log = LoggerFactory.getLogger(javaClass)


    fun create(documents: Sequence<Document>) {
        val memoryIndex = mutableMapOf<String, MutableMap<String, MutableSet<String>>>()
        for ((index, doc) in documents.withIndex()) {

            if (index % 100000 == 0) log.info("documents processed: {}", index)
            val windows = create(doc)
            for (res in windows) {
                if (!memoryIndex.containsKey(res.middleToken)) {
                    memoryIndex[res.middleToken] = mutableMapOf()
                }
                val windowsMap = memoryIndex[res.middleToken]!!
                val window = res.convertContentToString()
                if(windowsMap.containsKey(window)) {
                    //add doc id to existing element
                    windowsMap[window]!!.add(doc.id)
                }
                else {
                    windowsMap[window] = mutableSetOf(doc.id)
                }
            }
        }

        memoryIndex.forEach { (token, windows) ->
            windows.forEach { (window, docIds) ->
                val docStr = docIds.joinToString(" ")
                luceneService.save(LuceneDocument(token, docStr, window))
            }
        }

        luceneService.completeIndexing()
    }

    fun create(document: Document): List<Window> {
        return preprocessingPipelineExecutor.execute(document.content)
    }

    companion object {
        const val MAX_CONCURRENCY = 3
        const val DOC_BATCH_SIZE = 100_000
    }
}
