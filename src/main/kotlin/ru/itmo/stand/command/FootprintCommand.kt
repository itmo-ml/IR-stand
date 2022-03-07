package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import ru.itmo.stand.service.DocumentBm25Service

@Component
@Command(name = "footprint", description = ["Return footprint for index."])
class FootprintCommand(private val documentBm25Service: DocumentBm25Service) : Runnable {

    override fun run() {
        println("Footprint:\n${documentBm25Service.getFootprint()}")
    }
}
