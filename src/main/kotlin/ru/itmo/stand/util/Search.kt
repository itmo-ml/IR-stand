package ru.itmo.stand.util

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BoostQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import java.io.File

fun buildBagOfWordsQuery(field: String, analyzer: Analyzer, queryText: String): Query {
    val tokens = analyze(analyzer, queryText)

    val countByTokenMap = tokens.groupingBy { it }.eachCount()

    val builder = BooleanQuery.Builder()
    for ((token, count) in countByTokenMap) {
        builder.add(
            BoostQuery(TermQuery(Term(field, token)), count.toFloat()),
            BooleanClause.Occur.SHOULD,
        )
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
