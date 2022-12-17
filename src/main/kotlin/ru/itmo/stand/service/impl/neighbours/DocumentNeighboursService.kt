package ru.itmo.stand.service.impl.neighbours

import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.impl.neighbours.indexing.ContextualizedVectorCreator
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.util.extractId
import java.io.File

@Service
class DocumentNeighboursService(
    private val contextualizedVectorCreator: ContextualizedVectorCreator,
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
        TODO("Not yet implemented")
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        contextualizedVectorCreator.create(contents.map { extractId(it) })
        TODO("Not yet implemented")
    }

    override fun getFootprint(): String {
        TODO("Not yet implemented")
    }
}
