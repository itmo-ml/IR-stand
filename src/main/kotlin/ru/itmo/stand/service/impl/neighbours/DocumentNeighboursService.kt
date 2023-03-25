package ru.itmo.stand.service.impl.neighbours

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.impl.neighbours.indexing.VectorIndexBuilder
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.util.extractId
import java.io.File

@Service
class DocumentNeighboursService(
    private val windowedTokenCreator: WindowedTokenCreator,
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

    override fun saveInBatch(contents: Sequence<String>, withId: Boolean): List<String> {
        val windowedTokensFile = windowedTokenCreator.create(
            contents
                .take(standProperties.app.neighboursAlgorithm.documentsCount)
                .map { extractId(it) },
        )
        val meanClusters = vectorIndexBuilder.index(windowedTokensFile)

        // log.info("mean cluster size is $meanClusters")

        return emptyList()
    }

    override fun getFootprint(): String {
        TODO("Not yet implemented")
    }
}
