package ru.itmo.stand

import CustomEmbeddingTranslatorFactory
import ai.djl.repository.zoo.Criteria
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import ru.itmo.stand.config.StandProperties
import java.nio.file.Paths

@SpringBootApplication
@EnableConfigurationProperties(StandProperties::class)
class StandApplication

fun main(args: Array<String>) {
    val tinyModel = Criteria.builder()
        .setTypes(Array<String>::class.java, Array<FloatArray>::class.java)
        .optModelName("prajjwal1/bert-tiny")
        .optModelPath(Paths.get("./models/bert-tiny"))
        .optEngine("PyTorch")
        .optArgument("padding", "true")
        .optArgument("normalize", "false")
        .optArgument("pooling", "token")
        .optArgument("maxLength", "20")
        .optTranslatorFactory(CustomEmbeddingTranslatorFactory())
        .build()
        .loadModel()

//    exitProcess(SpringApplication.exit(runApplication<StandApplication>(*args)))
}

/*
TODO:
1. индексайция
2. бэнчмарки - https://arxiv.org/pdf/2105.04021.pdf, https://github.com/castorini/anserini

по
 */
