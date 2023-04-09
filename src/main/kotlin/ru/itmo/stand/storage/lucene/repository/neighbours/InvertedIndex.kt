package ru.itmo.stand.storage.lucene.repository.neighbours

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field.Store.YES
import org.apache.lucene.document.StringField
import org.apache.lucene.index.ConcurrentMergeScheduler
import org.apache.lucene.index.IndexWriterConfig
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.lucene.LuceneRepository
import ru.itmo.stand.storage.lucene.model.neighbours.NeighboursDocument

@Repository
class InvertedIndex(private val standProperties: StandProperties) : LuceneRepository() {
    override val indexPath: String
        get() = "${standProperties.app.basePath}/indexes/neighbours/document"
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
}
