package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.command.converter.DocumentBm25DtoConverter
import ru.itmo.stand.dto.DocumentBm25Dto
import ru.itmo.stand.service.DocumentBm25Service

@Component
@Command(
    name = "save",
    mixinStandardHelpOptions = true,
    description = ["Save the document and return its ID."],
)
class SaveCommand(private val documentBm25Service: DocumentBm25Service) : Runnable {

    @Parameters(
        paramLabel = "document",
        arity = "1",
        description = ["The content of the document to save."],
        converter = [DocumentBm25DtoConverter::class],
    )
    private lateinit var dto: DocumentBm25Dto

    override fun run() {
        println("Saved document ID: ${documentBm25Service.save(dto)}")
    }
}
