package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine
import picocli.CommandLine.Command
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService

@Component
@Command(
    name = "footprint",
    mixinStandardHelpOptions = true,
    description = ["Return footprint for index."],
)
class FootprintCommand(private val documentServicesByMethod: Map<Method, DocumentService>) : Runnable {

    @CommandLine.Option(names = ["-m", "-method"], required = true)
    private lateinit var method: Method

    override fun run() {
        println("Footprint: ${documentServicesByMethod[method]!!.getFootprint()}")
    }
}
