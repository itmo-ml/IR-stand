package ru.itmo.stand.command

import java.io.File
import java.nio.file.Files
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.measureTimeSeconds

@Component
@Command(
    name = "save-in-batch",
    mixinStandardHelpOptions = true,
    description = ["Save the documents and return their IDs."],
)
class SaveInBatchCommand(private val documentServicesByMethod: Map<Method, DocumentService>) : Runnable {

    @Parameters(
        paramLabel = "documents file",
        arity = "1",
        description = ["File with content of the documents to save."],
    )
    private lateinit var contentFile: File

    @Option(
        names = ["-m", "-method"],
        required = true,
        description = ["Search method. Available values: BM25, SNRM."]
    )
    private lateinit var method: Method

    @Option(
        names = ["--with-id"],
        description = ["Indicates that document has its own id, separated by tab"]
    )
    private var withId: Boolean = false

    override fun run() {
        val contents = Files.lines(contentFile.toPath()).toList()
        val seconds = measureTimeSeconds {
            println("Saved document IDs: ${documentServicesByMethod[method]!!.saveInBatch(contents, withId)}")
        }
        println("Latency: $seconds seconds")
    }
}
