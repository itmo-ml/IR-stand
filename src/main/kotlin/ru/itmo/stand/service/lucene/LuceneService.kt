package ru.itmo.stand.service.lucene

import kotlinx.coroutines.yield
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.SortedDocValuesField
import org.apache.lucene.document.StoredField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.FieldDoc
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.search.Sort
import org.apache.lucene.search.grouping.GroupingSearch
import org.apache.lucene.search.grouping.TopGroups
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef
import org.springframework.stereotype.Service
import ru.itmo.stand.config.StandProperties
import java.io.Closeable
import java.nio.file.Paths

@Service
class LuceneService (
    private val standProperties: StandProperties
) : Closeable {

    private val analyzer = StandardAnalyzer()
    private val indexWriterConfig = IndexWriterConfig(analyzer)

    private val indexDir = FSDirectory.open(Paths.get("${standProperties.app.basePath}/indexes/lucene"))

    private val writer = IndexWriter(indexDir, indexWriterConfig)

    private val searcher = IndexSearcher(DirectoryReader.open(indexDir))

    fun save(document: LuceneDocument) {
        saveInBatch(listOf(document))
    }

    fun saveInBatch(documents: List<LuceneDocument>) {
        val docs = documents.map() {
            val doc = Document()
            doc.add(SortedDocValuesField(GROUPING_KEY, BytesRef(it.groupKey)))
            doc.add(TextField(CONTENT, it.content, Field.Store.YES))
            doc.add(StoredField(DOC_ID, it.documentId))
            doc
        }

        writer.addDocuments(docs)

    }


    fun iterateTokens(): Sequence<Pair<String, List<LuceneDocument>>> {

        val grouping = createGrouping()
        var offset = 0
        return sequence {
            val searcher = IndexSearcher(DirectoryReader.open(indexDir))
            do {
                val searchResult: TopGroups<BytesRef> = grouping
                    .search(searcher, MatchAllDocsQuery(), 0, 100)

                val yieldResult = searchResult.groups.map {
                    val key = it.groupValue.utf8ToString()
                    val documents = it.scoreDocs.map { doc ->
                        val fields = searcher.doc(doc.doc)
                        val content = fields.get(CONTENT)
                        val docId = fields.get(DOC_ID)
                        LuceneDocument(key, docId, content)
                    }
                    key to documents
                }
                yieldAll(yieldResult)
                offset += GROUPING_LIMIT

            } while(searchResult.groups.isNotEmpty())
        }
    }


    fun clearIndex() {
        writer.deleteAll()
        writer.commit()
    }

    fun completeIndexing() {
        writer.forceMerge(1, true)
        writer.commit()
    }

    override fun close() {
        writer.close()
        indexDir.close()
    }

    private fun createGrouping(): GroupingSearch {
        val groupingSearch = GroupingSearch(GROUPING_KEY)
        groupingSearch.setAllGroups(true)
        groupingSearch.setGroupDocsOffset(0)
        groupingSearch.setGroupDocsLimit(GROUP_LIMIT)
        return groupingSearch
    }

    companion object {
        const val GROUPING_KEY= "groupingKey"
        const val CONTENT = "content"
        const val DOC_ID = "docId"
        const val GROUP_LIMIT = 2_000_000
        const val GROUPING_LIMIT = 5

    }
}