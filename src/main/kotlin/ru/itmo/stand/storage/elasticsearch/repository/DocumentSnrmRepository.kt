package ru.itmo.stand.storage.elasticsearch.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.storage.elasticsearch.model.DocumentSnrm

interface DocumentSnrmRepository : ElasticsearchRepository<DocumentSnrm, String> {

    @Query(
        """
        {
          "match": {
            "representation": "?0"
          }
        }""",
    ) // TODO: add constant_score
    fun findByRepresentation(representation: String, pageable: Pageable): Page<DocumentSnrm>
}
