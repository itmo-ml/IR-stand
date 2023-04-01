package ru.itmo.stand

import CustomEmbeddingTranslatorFactory
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.repository.zoo.Criteria
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.bert.CustomTranslatorInput
import java.nio.file.Paths

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
