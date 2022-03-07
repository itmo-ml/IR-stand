package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.command.converter.DocumentBm25DtoConverter
import ru.itmo.stand.dto.DocumentBm25Dto
import ru.itmo.stand.service.DocumentBm25Service

@Component
@Command(
    name = "save-in-batch",
    mixinStandardHelpOptions = true,
    description = ["Save the documents and return their IDs."],
)
class SaveInBatchCommand(private val documentBm25Service: DocumentBm25Service) : Runnable {

    @Parameters(
        paramLabel = "documents",
        arity = "2..*",
        description = ["The content of the documents to save. At least two documents."],
        converter = [DocumentBm25DtoConverter::class],
    )
    private lateinit var dtoList: List<DocumentBm25Dto>

    override fun run() {
        println("Saved document IDs: ${documentBm25Service.saveInBatch(dtoList)}")
    }
}
