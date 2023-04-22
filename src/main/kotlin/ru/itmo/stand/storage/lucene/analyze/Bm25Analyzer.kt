package ru.itmo.stand.storage.lucene.analyze

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.StopFilter
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.miscellaneous.CapitalizationFilter
import org.apache.lucene.analysis.standard.StandardTokenizer

class Bm25Analyzer : Analyzer() {

    override fun createComponents(fieldName: String?): TokenStreamComponents {
        val src = StandardTokenizer()
        var result: TokenStream = src
        result = LowerCaseFilter(result)
        result = StopFilter(result, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET)
        result = PorterStemFilter(result)
        result = CapitalizationFilter(result)
        return TokenStreamComponents(src, result)
    }
}
