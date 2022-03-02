package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import ru.itmo.stand.service.DocumentService

@Component
@Command(name = "footprint", description = ["Return footprint for index."])
class FootprintCommand(private val documentService: DocumentService) : Runnable {

    override fun run() {
        println("Footprint: ${documentService.getFootprint()}")
    }
}
