package ru.itmo.stand.storage.lucene.repository

import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.ConcurrentMergeScheduler
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.similarities.BM25Similarity
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.storage.lucene.LuceneRepository
import ru.itmo.stand.storage.lucene.analyze.Bm25Analyzer
import ru.itmo.stand.storage.lucene.model.DocumentBm25
import ru.itmo.stand.util.buildBagOfWordsQuery

@Repository
class DocumentBm25Repository(private val standProperties: StandProperties) : LuceneRepository() {

    override val indexPath: String
        get() = "${standProperties.app.basePath}/indexes/bm25"

    override val writerConfig: IndexWriterConfig
        get() = IndexWriterConfig(Bm25Analyzer()).apply {
            similarity = BM25Similarity()
            openMode = IndexWriterConfig.OpenMode.CREATE
            ramBufferSizeMB = 2048.0
            useCompoundFile = false
            mergeScheduler = ConcurrentMergeScheduler()
        }

    fun findByContent(content: String, count: Int): List<DocumentBm25> {
        val queryWithBagOfWords = buildBagOfWordsQuery(DocumentBm25::content.name, Bm25Analyzer(), content)

        val topDocs = searcher.search(queryWithBagOfWords, count)
        return topDocs.scoreDocs
            .map { searcher.storedFields().document(it.doc) }
            .map {
                DocumentBm25(
                    id = it.get(DocumentBm25::id.name),
                    content = it.get(DocumentBm25::content.name),
                )
            }
    }

    fun save(document: DocumentBm25) {
        saveAll(listOf(document))
    }

    fun saveAll(documents: Collection<DocumentBm25>) {
        documents.forEach {
            val doc = Document()
            doc.add(StringField(DocumentBm25::id.name, it.id, Field.Store.YES))
            doc.add(TextField(DocumentBm25::content.name, it.content, Field.Store.YES))
            writer.addDocument(doc)
        }
    }

    fun completeSaving() {
        writer.forceMerge(1, true)
        writer.commit()
    }
}
