package ru.itmo.stand.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.service.DocumentService

@RestController
@RequestMapping("/api/document")
class DocumentController(private val documentService: DocumentService) {

    @GetMapping("/")
    fun searchDocument(@RequestParam query: String): ResponseEntity<List<String>> {
        val dto = documentService.search(query)
        return if (dto.isEmpty()) ResponseEntity.notFound().build()
        else ResponseEntity.ok(dto)
    }

    @PostMapping("/")
    fun indexDocument(@RequestBody dto: DocumentDto) = documentService.index(dto)

}
