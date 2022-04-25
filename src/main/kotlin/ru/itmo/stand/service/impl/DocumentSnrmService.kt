package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.measureTimeMillis
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.tensorflow.SavedModelBundle
import org.tensorflow.Tensor
import ru.itmo.stand.cache.repository.DocumentVectorRepository
import ru.itmo.stand.cache.repository.TermRepository
import ru.itmo.stand.cache.repository.TokenCounterRepository
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params.BATCH_SIZE_DOCUMENTS
import ru.itmo.stand.config.Params.MAX_DOC_LEN
import ru.itmo.stand.index.InMemoryIndex
import ru.itmo.stand.index.model.DocumentSnrm
import ru.itmo.stand.index.repository.DocumentSnrmRepository
import ru.itmo.stand.service.DocumentService

@Service
class DocumentSnrmService(
    private val documentSnrmRepository: DocumentSnrmRepository,
    private val stanfordCoreNlp: StanfordCoreNLP,
    private val inMemoryIndex: InMemoryIndex,
    private val documentVectorRepository: DocumentVectorRepository,
    private val tokenCounterRepository: TokenCounterRepository,
    private val termRepository: TermRepository
) : DocumentService {

    private val modelOutputSize = 5000;
    private val tokenPrefix = "word";
    private val log = LoggerFactory.getLogger(javaClass)
    private val model = SavedModelBundle.load("src/main/resources/models/snrm/frozen", "serve")
    private val stopwords = Files.lines(Paths.get("src/main/resources/data/stopwords.txt")).toList().toSet()
    private val termToId = mutableMapOf("UNKNOWN" to 0).also {
        var id = 1
        Files.lines(Paths.get("src/main/resources/data/tokens.txt")).forEach { term -> it[term] = id++ }
    }

    override val method: Method
        get() = Method.SNRM

    override fun find(id: String): String? = documentSnrmRepository.findByIdOrNull(id)?.content

    override fun search(query: String): List<String> {
        val processedQuery = preprocess(listOf(query))[0].joinToString(" ")
        return documentSnrmRepository.findByRepresentation(processedQuery)
            .map { it.id ?: throwDocIdNotFoundEx() }
    }


    override fun save(content: String, withId: Boolean): String {

        val (externalId, passage) = extractId(content, withId);

        val representation = preprocess(listOf(passage))[0];

        //generate tokens word1, word2, word3
        val latentTermMap = representation
            .associateBy({ it }, { tokenPrefix + tokenCounterRepository.getNext() })
            .toMutableMap();

        //save them in redis
        termRepository.saveTerms(latentTermMap)

        //save document in elastic
        val tokenRepresentation = latentTermMap.values.joinToString(" ");
        val docId = documentSnrmRepository.save(
            DocumentSnrm(content = content, representation = tokenRepresentation, externalId = externalId)
        ).id ?: throwDocIdNotFoundEx()

        //save document in redis with id from elastic
        documentVectorRepository.saveDoc(docId, representation);

        return docId;
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> {
        log.info("Total size: ${contents.size}")

        contents.asSequence()
            .chunked(BATCH_SIZE_DOCUMENTS)
            .forEach { chunk ->

                val idsAndPassages = chunk.map { extractId(it, withId) }
                val ids = idsAndPassages.map { it.first }
                val passages = idsAndPassages.map { it.second }
                val representations = preprocess(passages);

                val latentTermMaps = representations.map {
                    it.associateBy({ it2 -> it2 }, { tokenPrefix + tokenCounterRepository.getNext() })
                        .toMutableMap()
                }

                latentTermMaps.forEach { termRepository.saveTerms(it) }

                val documents = List(representations.size) { idx ->
                    val representation = latentTermMaps[idx].values.joinToString(" ")
                    DocumentSnrm(content = passages[idx], externalId = ids[idx], representation = representation)
                }
                val savedDocs = documentSnrmRepository.saveAll(documents).toList()

                val documentVectors = representations.mapIndexed { idx, value ->
                    val id = savedDocs[idx].id ?: throwDocIdNotFoundEx()
                    Pair(id, value)
                }.associateBy({it.first}, {it.second}).toMutableMap()

                documentVectorRepository.saveDocs(documentVectors)

            }


        return emptyList()
    }

    override fun getFootprint(): String? {
        TODO("Not yet implemented")
    }


    private fun preprocess(contents: List<String>): List<FloatArray> {
        // create session
        val sess = model.session()

        // tokenization
        val tokens: List<List<String>> = contents.map {
            stanfordCoreNlp.processToCoreDocument(it)
                .tokens()
                .map { it.lemma().lowercase() }
        }

        // form term id list
        val termIds: List<MutableList<Int>> = tokens.map {
            val temp = it.filter { !stopwords.contains(it) }
                .map { if (termToId.containsKey(it)) termToId[it]!! else termToId["UNKNOWN"]!! }
                .toMutableList()
            // fill until max doc length or trim for it
            for (i in 1..(MAX_DOC_LEN - temp.size)) temp.add(0)
            temp.subList(0, MAX_DOC_LEN)
        }

        // create tensor
        val x = Tensor.create(termIds.map { it.toIntArray() }.toTypedArray())

        // inference
        val y = sess.runner()
            .feed("Placeholder_4", x)
            .fetch("Mean_5")
            .run()[0]

        val initArray = Array(contents.size) { FloatArray(modelOutputSize) }

        return y.copyTo(initArray).map { arr ->
            arr.filter { it != 0.0f }
                .toFloatArray()
        }
    }
}
