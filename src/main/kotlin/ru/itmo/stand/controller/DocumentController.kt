package ru.itmo.stand.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.service.DocumentService

@RestController
@RequestMapping("/api/documents")
class DocumentController(private val documentService: DocumentService) {

    @GetMapping("/")
    fun searchDocument(@RequestParam query: String) = documentService.search(query).let {
        if (it.isEmpty()) ResponseEntity.notFound().build()
        else ResponseEntity.ok(it)
    }

    @GetMapping("/{id}")
    fun findDocument(@PathVariable id: String) = documentService.find(id).let {
        if (it == null) ResponseEntity.notFound().build()
        else ResponseEntity.ok(it)
    }

    @PostMapping("/")
    fun saveDocument(@RequestBody dto: DocumentDto) =
        documentService.save(dto)

    @PostMapping("/batch")
    fun saveDocuments(@RequestBody dtoList: List<DocumentDto>) =
        documentService.saveBatch(dtoList)

    @GetMapping("/footprint")
    fun getFootprint() = documentService.getFootprint().let {
        if (it == null) ResponseEntity.notFound().build()
        else ResponseEntity.ok(it)
    }

}
