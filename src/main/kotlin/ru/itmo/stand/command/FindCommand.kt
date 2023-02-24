package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "find",
    mixinStandardHelpOptions = true,
    description = ["Return the document found by ID."],
)
class FindCommand(private val documentServicesByMethod: Map<Method, DocumentService>) : Runnable {

    @Parameters(
        paramLabel = "id",
        arity = "1",
        description = ["ID by which the document is found."],
    )
    private lateinit var id: String

    @Option(names = ["-m", "-method"], required = true)
    private lateinit var method: Method

    override fun run() {
        println(documentServicesByMethod[method]!!.find(id) ?: "Document not found.")
    }
}
