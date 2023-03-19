package ru.itmo.stand.service.impl.neighbours.indexing

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.impl.neighbours.PreprocessingPipelineExecutor
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.util.Window
import ru.itmo.stand.util.createPath
import java.io.File

@Service
class WindowedTokenCreator(
    private val preprocessingPipelineExecutor: PreprocessingPipelineExecutor,
    private val standProperties: StandProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun create(documents: Sequence<Document>): File {
        val memoryIndex = constructMemoryIndex(documents)

        log.info("memoryIndex is constructed. Token number: ${memoryIndex.size}")
        log.info("min windows: ${memoryIndex.values.minBy { it.keys.size }.keys.size}")
        log.info("max windows: ${memoryIndex.values.maxBy { it.keys.size }.keys.size}")
        log.info("mean windows: ${memoryIndex.values.map { it.keys.size }.average()}")

        val windowedTokensFile = File("${standProperties.app.basePath}/indexes/neighbours/windowed-tokens.txt")
            .createPath()

        writeMemoryIndexToFile(memoryIndex, windowedTokensFile)

        return windowedTokensFile
    }

    private fun constructMemoryIndex(documents: Sequence<Document>): HashMap<String, HashMap<String, String>> {
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
        return memoryIndex
    }

    private fun writeMemoryIndexToFile(
        memoryIndex: HashMap<String, HashMap<String, String>>,
        windowedTokensFile: File,
    ) {
        windowedTokensFile.bufferedWriter()
            .use { out ->
                memoryIndex.forEach { (token, windows) ->
                    out.write(token)
                    out.write("=")
                    windows.forEach { (window, docIds) ->
                        out.write(window)
                        out.write(":")
                        out.write(docIds)
                        out.write(";")
                    }
                    out.newLine()
                }
            }
    }

    fun create(document: Document): List<Window> {
        return preprocessingPipelineExecutor.execute(document.content)
    }
}
