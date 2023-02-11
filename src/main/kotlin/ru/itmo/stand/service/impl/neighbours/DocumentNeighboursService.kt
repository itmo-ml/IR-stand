package ru.itmo.stand.service.impl.neighbours

import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.impl.neighbours.indexing.WindowedTokenCreator
import ru.itmo.stand.service.lucene.LuceneService
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.util.extractId
import java.io.File

@Service
class DocumentNeighboursService(
    private val windowedTokenCreator: WindowedTokenCreator,
    private val luceneService: LuceneService
) : DocumentService {
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

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        luceneService.clearIndex()

        windowedTokenCreator.create(contents.map { extractId(it) })

        luceneService.iterateTokens().forEach {
            println(it.first)
            println(it.second.size)
            println(it.second[0])
        }
        TODO("Not yet implemented")
    }

    override fun getFootprint(): String {
        TODO("Not yet implemented")
    }
}
