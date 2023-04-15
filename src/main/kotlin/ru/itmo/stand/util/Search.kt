package ru.itmo.stand.util

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BoostQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery

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
