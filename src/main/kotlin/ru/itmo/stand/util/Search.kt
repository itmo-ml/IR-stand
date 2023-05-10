package ru.itmo.stand.util

import io.github.oshai.KotlinLogging
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BoostQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import java.io.File

private val log = KotlinLogging.logger { }

fun IndexSearcher.searchAll(query: Query): Sequence<Document> {
    val pageSize = 100_000
    var topDocs = this.search(query, pageSize)
    log.debug { "Found ${topDocs.totalHits.value} hits" }
    return sequence {
        // java doc: The returned instance should only be used by a single thread.
        val storedFields = this@searchAll.storedFields()
        while (topDocs.scoreDocs.isNotEmpty()) {
            for (scoreDoc in topDocs.scoreDocs) {
                yield(storedFields.document(scoreDoc.doc))
            }
            val lastScoreDoc = topDocs.scoreDocs.last()
            topDocs = this@searchAll.searchAfter(lastScoreDoc, query, pageSize)
        }
    }
}

fun buildBagOfWordsQuery(field: String, analyzer: Analyzer, queryText: String): Query {
    val tokens = analyze(analyzer, queryText)

    val countByTokenMap = tokens.groupingBy { it }.eachCount()

    return booleanQuery(countByTokenMap.entries) { (token, count) ->
        BoostQuery(TermQuery(Term(field, token)), count.toFloat())
    }
}

fun <E> booleanQuery(source: Collection<E>, queryConverter: (E) -> Query): BooleanQuery {
    val builder = BooleanQuery.Builder()
    for (e in source) {
        builder.add(queryConverter(e), BooleanClause.Occur.SHOULD)
    }
    return builder.build()
}

typealias DocId = String

fun writeAsFileInMrrFormat(
    queries: File,
    outputPath: String,
    searcher: (String) -> List<DocId>,
) {
    val queryByIdMap = getQueryByIdMap(queries)
    val outputLines = mutableListOf<String>()

    for ((queryId, query) in queryByIdMap) {
        val docsTopList = searcher(query)
            .mapIndexed { index, docId -> formatMrr(queryId, docId, index + 1) }
        outputLines.addAll(docsTopList)
    }

    File(outputPath).createPath()
        .bufferedWriter()
        .use { file -> outputLines.forEach { line -> file.appendLine(line) } }
}

private fun getQueryByIdMap(queries: File): Map<Int, String> = queries.bufferedReader().useLines { lines ->
    lines.filter { it.isNotEmpty() }
        .map { it.split("\t") }
        .associate { it[0].toInt() to it[1] }
}
