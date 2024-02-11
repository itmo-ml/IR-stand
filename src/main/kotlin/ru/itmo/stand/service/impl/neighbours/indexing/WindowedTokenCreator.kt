package ru.itmo.stand.service.impl.neighbours.indexing

import io.github.oshai.KotlinLogging
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

    private val log = KotlinLogging.logger { }

    fun create(documents: Sequence<Document>): File {
        val windowedTokensFile = File("${standProperties.app.basePath}/indexes/neighbours/windowed-tokens.txt")
        if (windowedTokensFile.exists()) {
            log.info {
                "The windowedTokensFile already exist. " +
                    "The addition of new tokens is not being executed. " +
                    "The creation process has been omitted. " +
                    "If you wish to add new ones, please remove the previous ones."
            }
            return windowedTokensFile
        }
        val memoryIndex = constructMemoryIndex(documents)

        log.info { "MemoryIndex is constructed. Token number: ${memoryIndex.size}" }
        log.info { "Min windows: ${memoryIndex.values.minBy { it.keys.size }.keys.size}" }
        log.info { "Max windows: ${memoryIndex.values.maxBy { it.keys.size }.keys.size}" }
        log.info { "Mean windows: ${memoryIndex.values.map { it.keys.size }.average()}" }

        writeMemoryIndexToFile(memoryIndex, windowedTokensFile.createPath())

        return windowedTokensFile
    }

    private fun constructMemoryIndex(documents: Sequence<Document>): HashMap<String, HashMap<String, String>> {
        val memoryIndex = hashMapOf<String, HashMap<String, String>>()

        for ((index, document) in documents.withIndex()) {
            if (index % 10000 == 0) log.info("Documents processed: {}", index)
            val docId = document.id
            val windows = create(document)
            for (window in windows) {
                if (!memoryIndex.containsKey(window.middleToken)) {
                    memoryIndex[window.middleToken] = hashMapOf()
                }
                val docIdsByContentMap = checkNotNull(memoryIndex[window.middleToken])
                val currentContent = window.joinIndexAndContent(WINDOW_TOKEN_INDEX_SEPARATOR)
                val docIds = docIdsByContentMap[currentContent]
                if (docIds == null) {
                    docIdsByContentMap[currentContent] = docId
                } else {
                    docIdsByContentMap[currentContent] = "$docIds$DOC_IDS_SEPARATOR$docId"
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
                    out.write(TOKEN_WINDOWS_SEPARATOR)
                    windows.forEach { (window, docIds) ->
                        out.write(window)
                        out.write(WINDOW_DOC_IDS_SEPARATOR)
                        out.write(docIds)
                        out.write(WINDOWS_SEPARATOR)
                    }
                    out.newLine()
                }
            }
    }

    fun create(document: Document): List<Window> {
        return preprocessingPipelineExecutor.execute(document.content)
    }

    companion object {
        const val TOKEN_WINDOWS_SEPARATOR = "="
        const val WINDOW_TOKEN_INDEX_SEPARATOR = "|"
        const val WINDOW_DOC_IDS_SEPARATOR = ":"
        const val WINDOWS_SEPARATOR = ";"
        const val DOC_IDS_SEPARATOR = " "
    }
}
