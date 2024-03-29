package ru.itmo.stand.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.measureTimeSeconds
import java.io.File

@Component
@Command(
    name = "save-in-batch",
    mixinStandardHelpOptions = true,
    description = ["Save the documents and return their IDs."],
)
class SaveInBatchCommand(
    private val documentService: DocumentService,
) : Runnable {

    @Parameters(
        paramLabel = "documents file",
        arity = "1",
        description = ["File with content of the documents to save."],
    )
    private lateinit var contentFile: File

    @Option(
        names = ["--with-id"],
        description = ["Indicates that document has its own id, separated by tab"],
    )
    private var withId: Boolean = false

    override fun run(): Unit = runBlocking(Dispatchers.Default) {
        val seconds = measureTimeSeconds {
            println("Saved document IDs: ${documentService.saveInBatch(contentFile, withId)}")
        }
        println("Latency: $seconds seconds")
    }
}
