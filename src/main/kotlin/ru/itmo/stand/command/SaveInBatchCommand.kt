package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.command.converter.DocumentDtoConverter
import ru.itmo.stand.dto.DocumentDto
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "save-in-batch",
    mixinStandardHelpOptions = true,
    description = ["Save the documents and return their IDs."],
)
class SaveInBatchCommand(private val documentService: DocumentService) : Runnable {

    @Parameters(
        paramLabel = "documents",
        arity = "2..*",
        description = ["The content of the documents to save. At least two documents."],
        converter = [DocumentDtoConverter::class],
    )
    private lateinit var dtoList: List<DocumentDto>

    override fun run() {
        println("Saved document IDs: ${documentService.saveInBatch(dtoList)}")
    }
}
