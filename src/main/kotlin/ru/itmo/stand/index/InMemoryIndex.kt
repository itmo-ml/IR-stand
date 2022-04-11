package ru.itmo.stand.index

import edu.stanford.nlp.io.IOUtils.readObjectFromFile
import edu.stanford.nlp.io.IOUtils.writeObjectToFile
import org.springframework.stereotype.Component
import ru.itmo.stand.config.Params.BASE_PATH
import ru.itmo.stand.config.Params.BATCH_SIZE_DOCUMENTS
import ru.itmo.stand.config.Params.INDEX_PATH
import ru.itmo.stand.config.Params.NUM_DOCUMENT_BATCHES
import ru.itmo.stand.config.Params.RUN_NAME

@Component
class InMemoryIndex(
    private val nLatentTerms: Int = 5000,
) {
    private val memmapNumRows: Int
    private val memmapNumCols: Int = nLatentTerms
    private val filenameMemmapIndex: String
    private val filenameLatentTermIndex: String
    private val filenameDocKeymappingIndex: String
    private var sequenceValue = 0
    private val index = mutableMapOf<Int, MutableList<Int>>()
    private val docIdToMemmapIdx = mutableMapOf<Int, Int>()
    private val docReprMemmap = mutableMapOf<Int, FloatArray>()

    init {
        memmapNumRows = BATCH_SIZE_DOCUMENTS * NUM_DOCUMENT_BATCHES
        val indexStoragePathPrefix = BASE_PATH + INDEX_PATH + RUN_NAME
        filenameMemmapIndex = "$indexStoragePathPrefix-memmap_docrepr_index"
        filenameLatentTermIndex = "$indexStoragePathPrefix-latent_term_index"
        filenameDocKeymappingIndex = "$indexStoragePathPrefix-doc_keymapping_index"
    }

    fun add(docIds: List<Int>, docReprs: List<FloatArray>) {
        for (i in docIds.indices) {
            var shouldAddDocToIndex = false
            val currentDocId = docIds[i]
            val currentDocRepr = docReprs[i]

            for (j in currentDocRepr.indices) {
                if (currentDocRepr[j] > 0.0) {
                    shouldAddDocToIndex = true
                    if (!index.containsKey(j)) {
                        index[j] = mutableListOf()
                    }
                    index[j]?.add(currentDocId)
                }
            }

            if (shouldAddDocToIndex && currentDocId !in docIdToMemmapIdx) {
                val memmapIndexForDoc = nextSequenceVal()
                docIdToMemmapIdx[currentDocId] = memmapIndexForDoc
                docReprMemmap[memmapIndexForDoc] = currentDocRepr
            }
        }
    }

    fun store() {
        writeObjectToFile(docReprMemmap, filenameMemmapIndex)
        docReprMemmap.clear()

        writeObjectToFile(docIdToMemmapIdx, filenameDocKeymappingIndex)
        docIdToMemmapIdx.clear()

        writeObjectToFile(index, filenameLatentTermIndex)
        index.clear()
    }

    fun load() {
        index.clear()
        index.putAll(readObjectFromFile<Map<Int, MutableList<Int>>>(filenameLatentTermIndex))

        docIdToMemmapIdx.clear()
        docIdToMemmapIdx.putAll(readObjectFromFile<Map<Int, Int>>(filenameDocKeymappingIndex))

        docReprMemmap.clear()
        docReprMemmap.putAll(readObjectFromFile<Map<Int, FloatArray>>(filenameMemmapIndex))
    }

    fun getDocRepresentation(docId: Int): FloatArray? {
        val memmmapDocReprIndex = docIdToMemmapIdx[docId]
        return docReprMemmap[memmmapDocReprIndex]
    }

    fun countDocuments() = docIdToMemmapIdx.size

    fun nextSequenceVal(): Int {
        val nextValue = sequenceValue
        sequenceValue += 1
        return nextValue
    }
}
