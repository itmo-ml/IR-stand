package ru.itmo.stand.index.repository

import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.index.model.DocumentBm25

interface DocumentBm25Repository : ElasticsearchRepository<DocumentBm25, String> {

    @Query(
        """
        {
          "match": {
            "content": "?0"
          }
        }"""
    )
    fun findByContent(content: String): List<DocumentBm25>

}
