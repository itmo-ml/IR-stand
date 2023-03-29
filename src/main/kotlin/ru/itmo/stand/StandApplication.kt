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
    val tinyModel = Criteria.builder()
        .setTypes(Array<CustomTranslatorInput>::class.java, Array<FloatArray>::class.java)
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

    val tokenizer = HuggingFaceTokenizer.newInstance(Paths.get("./models/bert-tiny/tokenizer.json"))

    val text = "Some input text for testing"
    val tokens = tokenizer.tokenize(text)

    val window = tokens.take(5).joinToString(" ")

    val predictor = tinyModel.newPredictor()
    val prediction = predictor.predict(arrayOf(CustomTranslatorInput(2, window)))

//    exitProcess(SpringApplication.exit(runApplication<StandApplication>(*args)))
}

/*
TODO:
1. индексайция
2. бэнчмарки - https://arxiv.org/pdf/2105.04021.pdf, https://github.com/castorini/anserini

по
 */
