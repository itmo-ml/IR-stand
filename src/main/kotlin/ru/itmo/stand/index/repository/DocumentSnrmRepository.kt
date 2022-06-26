package ru.itmo.stand.index.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.index.model.DocumentSnrm

interface DocumentSnrmRepository : ElasticsearchRepository<DocumentSnrm, String> {

    @Query(
        """
        {
          "match": {
            "representation": "?0"
          }
        }"""
    )
    fun findByRepresentation(representation: String, pageable: Pageable): Page<DocumentSnrm>
}
