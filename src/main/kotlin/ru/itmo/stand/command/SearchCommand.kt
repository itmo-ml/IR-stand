package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.bert.BertEmbeddingCalculator
import ru.itmo.stand.service.bert.TranslatorInput
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
class SearchCommand(
    private val documentService: DocumentService,
    private val bertEmbeddingCalculator: BertEmbeddingCalculator,
) : Runnable {

    @Parameters(
        paramLabel = "queries file",
        arity = "1",
        description = ["File with content of queries to search relevant documents."],
    )
    private lateinit var queries: File

    @Option(names = ["-f", "-format"])
    private var format: Format = JUST_QUERY

    override fun run() {
        val inputs = queries.bufferedReader().readLines().map { TranslatorInput.withClsWordIndex(it) }
        val aLotOfInputs = (1..10).flatMap { inputs }
        repeat(10) {
            bertEmbeddingCalculator.calculate(aLotOfInputs, 1000)
        }
        val latencyInSeconds = measureTimeSeconds {
            println(documentService.search(queries, format).ifEmpty { "Documents not found." })
        }
        println("Latency: $latencyInSeconds seconds")
    }
}
