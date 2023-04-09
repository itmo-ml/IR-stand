package ru.itmo.stand.storage.lucene.repository

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.index.ConcurrentMergeScheduler
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.TermQuery
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.lucene.LuceneRepository
import ru.itmo.stand.storage.lucene.model.NeighboursEmbedding
import ru.itmo.stand.util.toFloatArray

@Repository
class NeighboursEmbeddingRepository(private val standProperties: StandProperties) : LuceneRepository() {

    override val indexPath: String
        get() = "${standProperties.app.basePath}/indexes/neighbours/document_embedding"

    override val writerConfig: IndexWriterConfig
        get() = IndexWriterConfig(StandardAnalyzer()).apply {
            openMode = IndexWriterConfig.OpenMode.CREATE
            ramBufferSizeMB = 2048.0
            useCompoundFile = false
            mergeScheduler = ConcurrentMergeScheduler()
        }

    fun save(entity: NeighboursEmbedding) {
        val vectorString = buildString {
            for (fv in entity.embedding) {
                if (isNotEmpty()) {
                    append(' ')
                }
                append(fv)
            }
        }
        val document = Document()
        document.add(StringField("id", entity.docId, Field.Store.YES))
        document.add(StringField("vector", vectorString, Field.Store.YES))
        writer.addDocument(document)
    }

    fun saveAll(entities: Collection<NeighboursEmbedding>) {
        entities.forEach { save(it) }
    }

    fun findByDocId(docId: String): NeighboursEmbedding {
        val query = TermQuery(Term("id", docId))

        val topDocs = searcher.search(query, 10)
        return topDocs.scoreDocs
            .map { searcher.storedFields().document(it.doc) }
            .map { NeighboursEmbedding(it.get("id"), it.get("vector").toFloatArray()) }
            .single()
    }

    fun completeIndexing() {
        writer.forceMerge(1, true)
        writer.commit()
    }
}
