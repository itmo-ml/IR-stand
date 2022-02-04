package ru.itmo.stand

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class StandApplication

fun main(args: Array<String>) {
	runApplication<StandApplication>(*args)
}
