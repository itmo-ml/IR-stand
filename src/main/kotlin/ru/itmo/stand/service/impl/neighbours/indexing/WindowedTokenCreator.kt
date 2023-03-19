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
        val memoryIndex = hashMapOf<String, HashMap<String, String>>()

        for ((index, document) in documents.withIndex()) {
            if (index % 10000 == 0) log.info("documents processed: {}", index)
            val docId = document.id
            val windows = create(document)
            for (window in windows) {
                if (!memoryIndex.containsKey(window.middleToken)) {
                    memoryIndex[window.middleToken] = hashMapOf()
                }
                val docIdsByContentMap = memoryIndex[window.middleToken]!!
                val currentContent = window.convertContentToString()
                val contentAndDocIds = docIdsByContentMap[currentContent]
                if (contentAndDocIds == null) {
                    docIdsByContentMap[currentContent] = docId
                } else {
                    val docIds = docIdsByContentMap[currentContent]
                    docIdsByContentMap[currentContent] = "$docIds $docId"
                }
            }
        }

        log.info("memoryIndex is constructed. Token number: ${memoryIndex.size}")
        log.info("min windows: ${memoryIndex.values.minBy { it.keys.size }.keys.size}")
        log.info("max windows: ${memoryIndex.values.maxBy { it.keys.size }.keys.size}")
        log.info("mean windows: ${memoryIndex.values.map { it.keys.size }.average()}")

        memoryIndex.forEach { (token, windows) ->
            windows.forEach { (window, docIds) ->
                luceneService.save(LuceneDocument(token, docIds, window))
            }
        }

        luceneService.completeIndexing()
    }

    fun create(document: Document): List<Window> {
        return preprocessingPipelineExecutor.execute(document.content)
    }
}
