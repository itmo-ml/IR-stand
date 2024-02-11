package ru.itmo.stand.storage.lucene.repository.neighbours

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.StringField
import org.apache.lucene.index.ConcurrentMergeScheduler
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.TermQuery
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.lucene.LuceneRepository
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursEmbedding
import ru.itmo.stand.util.toFloatArray

@Repository
class DocumentEmbeddingRepository(private val standProperties: StandProperties) : LuceneRepository() {

    override val indexPath: String
        get() = "${standProperties.app.basePath}/indexes/neighbours/embedding"

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
        document.add(StringField(NeighboursEmbedding::docId.name, entity.docId, Field.Store.YES))
        document.add(StoredField(NeighboursEmbedding::embedding.name, vectorString))
        writer.addDocument(document)
    }

    fun saveAll(entities: Collection<NeighboursEmbedding>) {
        entities.forEach { save(it) }
    }

    fun findByDocId(docId: String): NeighboursEmbedding {
        val query = TermQuery(Term(NeighboursEmbedding::docId.name, docId))

        val topDocs = searcher.search(query, 10)
        return topDocs.scoreDocs
            .map { searcher.storedFields().document(it.doc) }
            .map {
                NeighboursEmbedding(
                    it.get(NeighboursEmbedding::docId.name),
                    it.get(NeighboursEmbedding::embedding.name).toFloatArray(),
                )
            }
            .single()
    }

    fun completeIndexing() {
        writer.forceMerge(1, true)
        writer.commit()
    }

    fun countAll(): Int = searcher.count(MatchAllDocsQuery())
}
