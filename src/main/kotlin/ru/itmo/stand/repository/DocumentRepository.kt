package ru.itmo.stand.repository

import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.model.Document

interface DocumentRepository : ElasticsearchRepository<Document, String> {

    @Query(
        """
        {
          "match": {
            "content": "?0"
          }
        }"""
    )
    fun findByContent(content: String): List<Document>

}
