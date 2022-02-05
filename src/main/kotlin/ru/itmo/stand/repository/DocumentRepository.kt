package ru.itmo.stand.repository

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import ru.itmo.stand.model.Document

interface DocumentRepository : ElasticsearchRepository<Document, String>
