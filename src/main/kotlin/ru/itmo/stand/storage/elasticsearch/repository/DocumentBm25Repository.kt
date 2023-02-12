package ru.itmo.stand.storage.elasticsearch.repository

import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.storage.elasticsearch.model.DocumentBm25

interface DocumentBm25Repository : ElasticsearchRepository<DocumentBm25, String> {

    @Query(
        """
        {
          "match": {
            "representation": "?0"
          }
        }"""
    )
    fun findByRepresentation(representation: String): List<DocumentBm25>

}
