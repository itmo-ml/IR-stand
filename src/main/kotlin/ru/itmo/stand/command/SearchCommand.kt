package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.stand.model.DocumentBm25
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "search",
    mixinStandardHelpOptions = true,
    description = ["Return IDs of documents relevant to the query."]
)
class SearchCommand(private val documentBm25Service: DocumentService) : Runnable {

    @Parameters(
        paramLabel = "query",
        arity = "1",
        description = ["Query to find relevant documents. Return their IDs."]
    )
    private lateinit var query: String

    override fun run() {
        println(documentBm25Service.search(query).ifEmpty { "Documents not found." })
    }
}
