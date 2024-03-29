package ru.itmo.stand.service.impl.neighbours

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.impl.neighbours.indexing.DocumentEmbeddingCreator
import ru.itmo.stand.service.impl.neighbours.indexing.InvertedIndexBuilder
import ru.itmo.stand.service.impl.neighbours.indexing.VectorIndexBuilder
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator
import ru.itmo.stand.service.impl.neighbours.search.NeighboursSearcher
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.writeAsFileInMrrFormat
import java.io.File

@Service
@ConditionalOnProperty(value = ["stand.app.method"], havingValue = "neighbours")
class DocumentNeighboursService(
    private val documentEmbeddingCreator: DocumentEmbeddingCreator,
    private val windowedTokenCreator: WindowedTokenCreator,
    private val invertedIndexBuilder: InvertedIndexBuilder,
    private val vectorIndexBuilder: VectorIndexBuilder,
    private val neighboursSearcher: NeighboursSearcher,
    private val standProperties: StandProperties,
) : DocumentService {

    override fun find(id: String): String? {
        TODO("Not yet implemented")
    }

    override fun search(queries: File, format: Format): List<String> = when (format) {
        Format.JUST_QUERY -> neighboursSearcher.search(queries.readLines().single())
        Format.MS_MARCO -> {
            val outputPath = "${standProperties.app.basePath}/outputs/" +
                "${standProperties.app.method.name.lowercase()}/resultInMrrFormat.tsv"
            writeAsFileInMrrFormat(queries, outputPath) { query -> neighboursSearcher.search(query) }
            listOf("See output in $outputPath")
        }
    }

    override fun save(content: String, withId: Boolean): String {
        windowedTokenCreator.create(extractId(content))
        TODO("Not yet implemented")
    }

    override suspend fun saveInBatch(contents: File, withId: Boolean): List<String> {
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
