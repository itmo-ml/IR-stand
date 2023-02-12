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

    private val indexDir by lazy {
        val indexPath = Paths.get("${standProperties.app.basePath}/indexes/lucene")
        FSDirectory.open(indexPath)
    }

    private val writer = IndexWriter(indexDir, indexWriterConfig)

    private val searcher = IndexSearcher(DirectoryReader.open(indexDir))

    fun save(w: WindowedToken) {
        saveInBatch(listOf(w))
    }

    fun saveInBatch(w: List<WindowedToken>) {
        val documents = w.map {
            val doc = Document()
            doc.add(SortedDocValuesField(TOKEN_FIELD, BytesRef(it.token)))
            doc.add(TextField(WINDOW_FIELD, it.window, Field.Store.YES))
            doc.add(StoredField(DOC_FIELD, it.documentId))

            doc
        }

        writer.addDocuments(documents)
    }


    fun iterateTokens(): Sequence<Pair<String, List<WindowedToken>>> {

        val grouping = createGrouping()
        var offset = 0
        return sequence {
            do {
                val searchResult: TopGroups<BytesRef> = grouping
                    .search(searcher, MatchAllDocsQuery(), offset, GROUPING_LIMIT)

                val yieldResult = searchResult.groups.map {
                    val token = it.groupValue.utf8ToString()
                    val windows = it.scoreDocs.map { doc ->
                        val fields = searcher.doc(doc.doc)
                        val window = fields.get(WINDOW_FIELD)
                        val docId = fields.get(DOC_FIELD)
                        WindowedToken(token, docId, window)
                    }
                    token to windows
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
        val groupingSearch = GroupingSearch(TOKEN_FIELD)
        groupingSearch.setAllGroups(true)
        groupingSearch.setGroupDocsOffset(0)
        groupingSearch.setGroupDocsLimit(GROUP_LIMIT)
        return groupingSearch
    }

    companion object {
        const val TOKEN_FIELD= "token"
        const val WINDOW_FIELD = "window"
        const val DOC_FIELD = "docId"
        const val GROUP_LIMIT = 2_000_000
        const val GROUPING_LIMIT = 100

    }
}