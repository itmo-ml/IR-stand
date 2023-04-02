package ru.itmo.stand.storage.lucene.repository

import jakarta.annotation.PreDestroy
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.ConcurrentMergeScheduler
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.similarities.BM25Similarity
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.preprocessing.TextCleaner
import ru.itmo.stand.storage.lucene.model.DocumentBm25
import java.nio.file.Paths

@Repository
class DocumentBm25Repository(
    private val textCleaner: TextCleaner,
    standProperties: StandProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val analyzer: StandardAnalyzer
    private val indexDir: FSDirectory
    private val writer: IndexWriter

    init {
        try {
            analyzer = StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET)
            indexDir = FSDirectory.open(Paths.get("${standProperties.app.basePath}/indexes/bm25"))
            val config = IndexWriterConfig(analyzer)
            config.similarity = BM25Similarity()
            config.openMode = IndexWriterConfig.OpenMode.CREATE
            config.ramBufferSizeMB = 2048.0
            config.useCompoundFile = false
            config.mergeScheduler = ConcurrentMergeScheduler()
            writer = IndexWriter(indexDir, config)
        } catch (ex: Exception) {
            log.error("Class initialization failed", ex)
            throw IllegalStateException(ex)
        }
    }

    private val searcher by lazy { IndexSearcher(DirectoryReader.open(indexDir)) }

    @PreDestroy
    private fun closeIndex() {
        indexDir.close()
    }

    fun findByContent(content: String, count: Int): List<DocumentBm25> {
        val query = QueryParser(DocumentBm25::content.name, analyzer)
            .parse(textCleaner.preprocess(content)) // TODO: try BagOfWords
        val topDocs = searcher.search(query, count)
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
