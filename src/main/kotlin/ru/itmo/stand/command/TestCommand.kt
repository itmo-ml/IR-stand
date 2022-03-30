package ru.itmo.stand.command

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.nio.IntBuffer
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.stereotype.Component
import org.tensorflow.SavedModelBundle
import org.tensorflow.Tensor
import picocli.CommandLine.Command

@Component
@Command(
    name = "test",
    mixinStandardHelpOptions = true,
    description = ["Temp command"]
)
class TestCommand(
    private val stanfordCoreNlp: StanfordCoreNLP,
) : Runnable {

    override fun run() {
        // load model
        val modelPath = "src/main/resources/models/snrm/frozen"
        val b = SavedModelBundle.load(modelPath, "serve")
        // create session
        val sess = b.session()

        // load termToId dictionary
        val termToId = mutableMapOf("UNKNOWN" to 0)
        var id = 1
        Files.lines(Paths.get("src/main/resources/data/tokens.txt")).forEach { termToId[it] = id++ }

        // load stopwords
        val stopwords = Files.lines(Paths.get("src/main/resources/data/stopwords.txt")).toList().toSet()

        // tokenization
        val tokens = stanfordCoreNlp.processToCoreDocument("""
            My name is Slava
            """.trimIndent())
            .tokens()
            .map { it.lemma().lowercase() }

        // form term id list
        val termIds = tokens.filter { !stopwords.contains(it) }
            .map { if (termToId.containsKey(it)) termToId[it]!! else termToId["UNKNOWN"]!! }
            .toMutableList()
        println("Test: $termIds")

        // fill until max doc length or trim for it
        val maxDocLength = 103
        for (i in 1..(maxDocLength - termIds.size)) termIds.add(0)
        val preparedTermIds: MutableList<Int> = termIds.subList(0, maxDocLength)

        // create tensor
        val x = Tensor.create(
            longArrayOf(maxDocLength.toLong()),
            IntBuffer.wrap(preparedTermIds.toIntArray())
        )

        // inference
        val y = sess.runner()
            .feed("Placeholder_4", x)
            .fetch("Mean_5")
            .run()[0]

        // print shape
        println(y.shape().contentToString())

        // print representation
        val representation = Array(1) { FloatArray(5000) }
//        val representation = Array(1) { Array(1) { Array(50) { FloatArray(5000) } } }
//        println(y.copyTo(representation).contentDeepToString())

        y.copyTo(representation)[0]
            .mapIndexed { index, fl -> Pair(index, fl) }
            .filter { it.second != 0.0f }
            .forEach {
                println("Idx: ${it.first}, Val: ${it.second}")
            }
    }
}
