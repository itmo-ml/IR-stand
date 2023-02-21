package ru.itmo.stand

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory
import ai.djl.repository.zoo.Criteria
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.itmo.stand.config.StandProperties
import kotlin.system.exitProcess
import java.nio.file.*;
import java.awt.image.*;
import ai.djl.*;
import ai.djl.inference.*;
import ai.djl.modality.*;
import ai.djl.modality.cv.*;
import ai.djl.modality.cv.util.*;
import ai.djl.modality.cv.transform.*;
import ai.djl.modality.cv.translator.*;
import ai.djl.repository.zoo.*;
import ai.djl.translate.*;
import ai.djl.training.util.*;
@SpringBootApplication
@EnableConfigurationProperties(StandProperties::class)
class StandApplication

private val defaultModel by lazy {
    Criteria.builder()
        .setTypes(Array<String>::class.java, Array<FloatArray>::class.java)
        .optModelName("prajjwal1/bert-tiny")
        .optModelPath(Paths.get("./models/bert-tiny"))
        .optEngine("PyTorch")
        .optArgument("padding", "true")
        .optArgument("normalize", "false")
        .optArgument("pooling", "cls")
        .optTranslatorFactory(TextEmbeddingTranslatorFactory())
        .build()
        .loadModel()

}


fun main(args: Array<String>) {
    val input = arrayOf(
        "sentence number one",
        "second sentence to process",
        "some other sentence with many words"
    )

    val predictor = defaultModel.newPredictor()

     val result = predictor.predict(input);
    for(res in result) {
        for(value in res) {
            print(value)
            print(", ")
        }
        println()
    }
}

/*
TODO:
1. индексайция
2. бэнчмарки - https://arxiv.org/pdf/2105.04021.pdf, https://github.com/castorini/anserini

по
 */
