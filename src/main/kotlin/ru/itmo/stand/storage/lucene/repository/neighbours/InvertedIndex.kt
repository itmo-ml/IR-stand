package ru.itmo.stand.storage.lucene.repository.neighbours

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field.Store.YES
import org.apache.lucene.document.StringField
import org.apache.lucene.index.ConcurrentMergeScheduler
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.TermQuery
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.lucene.LuceneRepository
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursDocument
import ru.itmo.stand.util.booleanQuery
import ru.itmo.stand.util.searchAll

@Repository
class InvertedIndex(private val standProperties: StandProperties) : LuceneRepository() {
    override val indexPath: String
        get() = "${standProperties.app.basePath}/indexes/neighbours/index"
    override val writerConfig: IndexWriterConfig
        get() = IndexWriterConfig(StandardAnalyzer()).apply {
            openMode = IndexWriterConfig.OpenMode.CREATE
            ramBufferSizeMB = 2048.0
            useCompoundFile = false
            mergeScheduler = ConcurrentMergeScheduler()
        }

    fun save(entity: NeighboursDocument) {
        val document = Document()
        document.add(StringField(NeighboursDocument::tokenWithEmbeddingId.name, entity.tokenWithEmbeddingId, YES))
        document.add(StringField(NeighboursDocument::docId.name, entity.docId, YES))
        document.add(StringField(NeighboursDocument::score.name, entity.score.toString(), YES))
        writer.addDocument(document)
    }

    fun saveAll(entities: Collection<NeighboursDocument>) {
        entities.forEach { save(it) }
    }

    fun findByTokenWithEmbeddingIds(tokenWithEmbeddingIds: Collection<String>): Sequence<NeighboursDocument> {
        val query = booleanQuery(tokenWithEmbeddingIds) { tokenWithEmbeddingId ->
            TermQuery(Term(NeighboursDocument::tokenWithEmbeddingId.name, tokenWithEmbeddingId))
        }

        return searcher.searchAll(query)
            .map {
                NeighboursDocument(
                    it.get(NeighboursDocument::tokenWithEmbeddingId.name),
                    it.get(NeighboursDocument::docId.name),
                    it.get(NeighboursDocument::score.name).toFloat(),
                )
            }
    }

    fun completeIndexing() {
        writer.forceMerge(1, true)
        writer.commit()
    }
}
