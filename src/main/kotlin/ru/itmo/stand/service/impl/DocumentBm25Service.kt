package ru.itmo.stand.service.impl

import io.github.oshai.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.storage.lucene.model.DocumentBm25
import ru.itmo.stand.storage.lucene.repository.DocumentBm25Repository
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.writeAsFileInMrrFormat
import java.io.File

@Service
class DocumentBm25Service(
    private val documentBm25Repository: DocumentBm25Repository,
    private val standProperties: StandProperties,
) : DocumentService {

    private val log = KotlinLogging.logger { }

    override val method: Method
        get() = Method.BM25

    override fun find(id: String): String? = TODO()

    override fun search(queries: File, format: Format): List<String> = when (format) {
        Format.JUST_QUERY -> documentBm25Repository.findByContent(queries.readLines().single(), 10).map { it.id }
        Format.MS_MARCO -> {
            val outputPath = "${standProperties.app.basePath}/outputs/${method.name.lowercase()}/resultInMrrFormat.tsv"
            writeAsFileInMrrFormat(queries, outputPath) { query ->
                documentBm25Repository.findByContent(query, 10).map { it.id }
            }
            listOf("See output in $outputPath")
        }
    }

    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (externalId, passage) = extractId(content)
        documentBm25Repository.save(DocumentBm25(id = externalId, content = passage))
        return "Document saved"
    }

    override fun saveInBatch(contents: File, withId: Boolean): List<String> {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val chunkSize = 10_000
        contents.lineSequence()
            .map { extractId(it) }
            .map { (externalId, passage) -> DocumentBm25(id = externalId, content = passage) }
            .chunked(chunkSize)
            .forEachIndexed { index, chunk ->
                documentBm25Repository.saveAll(chunk)
                log.info { "Processed: ${(index + 1) * chunkSize}" }
            }
        documentBm25Repository.completeSaving()
        return emptyList()
    }

    override fun getFootprint(): String = TODO()
}
