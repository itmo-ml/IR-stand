package ru.itmo.stand.repository

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.model.DocumentSnrm

interface DocumentSnrmRepository : ElasticsearchRepository<DocumentSnrm, String>
