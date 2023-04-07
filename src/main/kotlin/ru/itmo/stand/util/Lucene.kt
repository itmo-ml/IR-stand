package ru.itmo.stand.util

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BoostQuery
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import java.io.IOException
import java.io.StringReader

fun analyze(analyzer: Analyzer, s: String?): MutableList<String> {
    val list: MutableList<String> = ArrayList()
    try {
        val tokenStream = analyzer.tokenStream(null, StringReader(s))
        val cattr = tokenStream.addAttribute(CharTermAttribute::class.java)
        tokenStream.reset()
        while (tokenStream.incrementToken()) {
            if (cattr.toString().length == 0) {
                continue
            }
            list.add(cattr.toString())
        }
        tokenStream.end()
        tokenStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return list
}

fun buildBagOfWordsQuery(field: String, analyzer: Analyzer, queryText: String?): Query {
    val tokens = analyze(analyzer, queryText)

    val collect = tokens.groupingBy { it }.eachCount()

    val builder = BooleanQuery.Builder()
    for (t in collect.keys) {
        builder.add(
            BoostQuery(
                TermQuery(Term(field, t)),
                collect[t]!!
                    .toFloat(),
            ),
            BooleanClause.Occur.SHOULD,
        )
    }
    return builder.build()
}
