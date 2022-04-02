package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "save-in-batch",
    mixinStandardHelpOptions = true,
    description = ["Save the documents and return their IDs."],
)
class SaveInBatchCommand(private val documentBm25Service: DocumentService) : Runnable {

    @Parameters(
        paramLabel = "documents",
        arity = "2..*",
        description = ["The content of the documents to save. At least two documents."],
    )
    private lateinit var contents: List<String>

    override fun run() {
        println("Saved document IDs: ${documentBm25Service.saveInBatch(contents)}")
    }
}
