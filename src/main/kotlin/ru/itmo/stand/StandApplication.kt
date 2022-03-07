package ru.itmo.stand

import kotlin.system.exitProcess
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.itmo.stand.config.StandProperties


@SpringBootApplication
@EnableConfigurationProperties(StandProperties::class)
class StandApplication

fun main(args: Array<String>) {
    exitProcess(SpringApplication.exit(runApplication<StandApplication>(*args)))
}

/*
TODO:
1. индексайция
2. бэнчмарки - https://arxiv.org/pdf/2105.04021.pdf, https://github.com/castorini/anserini

по
 */
