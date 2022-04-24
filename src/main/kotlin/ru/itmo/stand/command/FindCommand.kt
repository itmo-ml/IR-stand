package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.index.model.DocumentBm25
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "find",
    mixinStandardHelpOptions = true,
    description = ["Return the document found by ID."],
)
class FindCommand(private val documentBm25Service: DocumentService) : Runnable {

    @Parameters(
        paramLabel = "id",
        arity = "1",
        description = ["ID by which the document is found."]
    )
    private lateinit var id: String

    override fun run() {
        println(documentBm25Service.find(id) ?: "Document not found.")
    }
}
