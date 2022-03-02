package ru.itmo.stand.command

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.stereotype.Component
import picocli.CommandLine
import picocli.CommandLine.IFactory

@Component
class StandCommandLineRunner(
    private val standCommand: StandCommand,
    private val iFactory: IFactory,
) : CommandLineRunner, ExitCodeGenerator {

    private var exitCode: Int = 0

    override fun run(vararg args: String) {
        exitCode = CommandLine(standCommand, iFactory).execute(*args)
    }

    override fun getExitCode(): Int {
        return exitCode
    }
}
