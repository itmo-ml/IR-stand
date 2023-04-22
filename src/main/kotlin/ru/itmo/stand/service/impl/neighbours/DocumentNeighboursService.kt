package ru.itmo.stand.service.impl.neighbours

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.impl.neighbours.indexing.DocumentEmbeddingCreator
import ru.itmo.stand.service.impl.neighbours.indexing.InvertedIndexBuilder
import ru.itmo.stand.service.impl.neighbours.indexing.VectorIndexBuilder
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.lineSequence
import java.io.File

@Service
class DocumentNeighboursService(
    private val documentEmbeddingCreator: DocumentEmbeddingCreator,
    private val windowedTokenCreator: WindowedTokenCreator,
    private val invertedIndexBuilder: InvertedIndexBuilder,
    private val vectorIndexBuilder: VectorIndexBuilder,
    private val standProperties: StandProperties,
) : DocumentService {

    private val log = LoggerFactory.getLogger(javaClass)

    override val method: Method
        get() = Method.NEIGHBOURS

    override fun find(id: String): String? {
        TODO("Not yet implemented")
    }

    override fun search(queries: File, format: Format): List<String> {
        TODO("Not yet implemented")
    }

    override fun save(content: String, withId: Boolean): String {
        windowedTokenCreator.create(extractId(content))
        TODO("Not yet implemented")
    }

    override fun saveInBatch(contents: File, withId: Boolean): List<String> {
        documentEmbeddingCreator.create(contents.documentSequenceWithSpecifiedCount())
        val windowedTokensFile = windowedTokenCreator.create(contents.documentSequenceWithSpecifiedCount())
        vectorIndexBuilder.index(windowedTokensFile)
        invertedIndexBuilder.index(windowedTokensFile)
        return emptyList()
    }

    override fun getFootprint(): String {
        TODO("Not yet implemented")
    }

    private fun File.documentSequenceWithSpecifiedCount() = this.lineSequence()
        .take(standProperties.app.neighboursAlgorithm.documentsCount)
        .map { extractId(it) }
}
