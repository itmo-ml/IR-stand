package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.measureTimeSeconds

@Component
@Command(
    name = "save",
    mixinStandardHelpOptions = true,
    description = ["Save the document and return its ID."],
)
class SaveCommand(private val documentServicesByMethod: Map<Method, DocumentService>) : Runnable {

    @Parameters(
        paramLabel = "document",
        arity = "1",
        description = ["The content of the document to save."],
    )
    private lateinit var content: String

    @Option(
        names = ["-m", "-method"],
        required = true,
        description = ["Search method. Available values: BM25, SNRM."],
    )
    private lateinit var method: Method

    @Option(
        names = ["--with-id"],
        description = ["Indicates that document has its own id, separated by tab"],
    )
    private var withId: Boolean = false

    override fun run() {
        val seconds = measureTimeSeconds {
            println("Saved document ID: ${documentServicesByMethod[method]!!.save(content, withId)}")
        }
        println("Latency: $seconds seconds")
    }
}
