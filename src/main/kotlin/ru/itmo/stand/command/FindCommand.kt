package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "find",
    mixinStandardHelpOptions = true,
    description = ["Return the document found by ID."],
)
class FindCommand(private val documentService: DocumentService) : Runnable {

    @Option(
        names = ["-i", "--id"],
        required = true,
        description = ["ID by which the document is found."]
    )
    private var id: String = ""

    override fun run() {
        println(documentService.find(id)?.content ?: "Document not found.")
    }
}
