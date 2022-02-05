package ru.itmo.stand.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.service.DocumentService

@RestController
@RequestMapping("/api/document")
class DocumentController(private val documentService: DocumentService) {

    @PostMapping("/")
    fun indexDocument(@RequestBody dto: DocumentDto) = documentService.indexDocument(dto)

}
