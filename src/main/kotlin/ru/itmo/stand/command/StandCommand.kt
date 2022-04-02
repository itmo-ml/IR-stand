package ru.itmo.stand.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command

@Component
@Command(
    name = "stand",
    mixinStandardHelpOptions = true,
    version = ["0.0.1-SNAPSHOT"],
    subcommands = [
        FootprintCommand::class,
        FindCommand::class,
        SearchCommand::class,
        SaveCommand::class,
        SaveInBatchCommand::class,
    ],
)
class StandCommand
