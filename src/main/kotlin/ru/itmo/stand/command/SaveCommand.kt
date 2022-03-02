package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.command.converter.DocumentDtoConverter
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "save",
    mixinStandardHelpOptions = true,
    description = ["Save the document and return its ID."],
)
class SaveCommand(private val documentService: DocumentService) : Runnable {

    @Parameters(
        paramLabel = "document",
        arity = "1",
        description = ["The content of the document to save."],
        converter = [DocumentDtoConverter::class],
    )
    private lateinit var dto: DocumentDto

    override fun run() {
        println("Saved document ID: ${documentService.save(dto)}")
    }
}
