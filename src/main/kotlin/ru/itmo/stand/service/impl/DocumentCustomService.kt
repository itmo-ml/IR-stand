package ru.itmo.stand.service.impl

import ai.djl.Application
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ZooModel
import ai.djl.training.util.ProgressBar
import java.nio.file.Paths
import org.springframework.stereotype.Service
import ru.itmo.stand.config.Method
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.dot


@Service
class DocumentCustomService : DocumentService() {
    override val method: Method
        get() = Method.CUSTOM

    override fun find(id: String): String? {
        TODO("Not yet implemented")
    }

    override fun search(query: String): List<String> {
        val translator = CustomTranslator()

        val criteria = Criteria.builder()
            .optApplication(Application.NLP.TEXT_EMBEDDING)
            .setTypes(String::class.java, FloatArray::class.java)
            .optModelPath(Paths.get("data/pytorch/bertqa/bert.pt")) // search in local folder
            .optTranslator(translator)
            .optProgress(ProgressBar())
            .build()

        val model: ZooModel<*, *> = criteria.loadModel()

        // Create a Predictor and use it to predict the output
        model.newPredictor(translator).use { predictor ->
            val queryRepr = predictor.predict("How many people live in London?")
            val doc1Repr = predictor.predict("Around 9 Million people live in London")
            val doc2Repr = predictor.predict("London is known for its financial district")

            println(queryRepr dot doc1Repr)
            println(queryRepr dot doc2Repr)
        }

        return emptyList()
    }

    override fun save(content: String, withId: Boolean): String {
        TODO("Not yet implemented")
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        TODO("Not yet implemented")
    }
}
