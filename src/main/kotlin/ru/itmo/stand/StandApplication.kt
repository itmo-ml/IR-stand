package ru.itmo.stand

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class StandApplication

fun main(args: Array<String>) {
	runApplication<StandApplication>(*args)
}

/*
TODO:
1. индексайция
2. бэнчмарки - https://arxiv.org/pdf/2105.04021.pdf, https://github.com/castorini/anserini

по
 */
