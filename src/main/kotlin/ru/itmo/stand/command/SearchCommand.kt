package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "search",
    mixinStandardHelpOptions = true,
    description = ["Return IDs of documents relevant to the query."]
)
class SearchCommand(private val documentService: DocumentService) : Runnable {

    @Option(
        names = ["-q", "--query"],
        required = true,
        description = ["Query to find relevant documents. Return their IDs."]
    )
    private var query: String = ""

    override fun run() {
        println(documentService.search(query))
    }
}
