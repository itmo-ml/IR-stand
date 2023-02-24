package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.service.model.Format.JUST_QUERY
import ru.itmo.stand.util.measureTimeSeconds
import java.io.File

@Component
@Command(
    name = "search",
    mixinStandardHelpOptions = true,
    description = ["Return IDs of documents relevant to the query."],
)
class SearchCommand(private val documentServicesByMethod: Map<Method, DocumentService>) : Runnable {

    @Parameters(
        paramLabel = "queries file",
        arity = "1",
        description = ["File with content of queries to search relevant documents."],
    )
    private lateinit var queries: File

    @Option(names = ["-m", "-method"], required = true)
    private lateinit var method: Method

    @Option(names = ["-f", "-format"])
    private var format: Format = JUST_QUERY

    override fun run() {
        val latencyInSeconds = measureTimeSeconds {
            println(documentServicesByMethod[method]!!.search(queries, format).ifEmpty { "Documents not found." })
        }
        println("Latency: $latencyInSeconds seconds")
    }
}
