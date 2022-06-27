package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.tensorflow.SavedModelBundle
import org.tensorflow.Tensor
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params.BATCH_SIZE_DOCUMENTS
import ru.itmo.stand.config.Params.MAX_DOC_LEN
import ru.itmo.stand.config.Params.SNRM_OUTPUT_SIZE
import ru.itmo.stand.index.model.DocumentSnrm
import ru.itmo.stand.index.repository.DocumentSnrmRepository
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.util.dot

@Service
class DocumentSnrmService(
    private val documentSnrmRepository: DocumentSnrmRepository,
    private val stanfordCoreNlp: StanfordCoreNLP,
) : DocumentService {

    private val log = LoggerFactory.getLogger(javaClass)
    // TODO: use absolute path and move to props
    private val model = SavedModelBundle.load("src/main/resources/models/snrm/frozen", "serve")
    private val stopwords = Files.lines(Paths.get("src/main/resources/data/stopwords.txt")).toList().toSet()
    private val termToId = mutableMapOf("UNKNOWN" to 0).also {
        var id = 1
        Files.lines(Paths.get("src/main/resources/data/tokens.txt")).forEach { term -> it[term] = id++ }
    }.toMap()

    override val method: Method
        get() = Method.SNRM

    override fun find(id: String): String? = documentSnrmRepository.findByIdOrNull(id)?.content

    override fun search(query: String): List<String> {
        val queryVector = preprocess(listOf(query))[0] // TODO: change network layer for query
        val documents = mutableListOf<DocumentSnrm>()
        val (latentTerms, weights) = retrieveLatentTermsAndWeights(queryVector)
        findDocsByTermsWithPages(documents, latentTerms, Pageable.ofSize(2000))

        val queryLatentTermWeightMap = convertToMap(latentTerms, weights)
        // TODO: think about improving the algorithm
        return documents.map {
            Pair(
                it.id ?: throwDocIdNotFoundEx(),
                dotProduct(queryLatentTermWeightMap, convertToMap(it.representation, it.weights))
            )
        }
            .sortedByDescending { it.second }
            .take(10) // TODO: make it a parameter
            .map { it.first }
    }

    private fun convertToMap(latentTerms: String, weights: FloatArray): Map<Int, Float> = latentTerms.split(" ")
        .map { it.toInt() }
        .withIndex()
        .associateBy({ it.value }, { weights[it.index] })

    private fun dotProduct(queryLatentTermWeightMap: Map<Int, Float>, documentLatentTermWeightMap: Map<Int, Float>): Double {
        var score = 0.0
        for (i in queryLatentTermWeightMap.keys) {
            val queryWeight = queryLatentTermWeightMap[i]
            val documentWeight = documentLatentTermWeightMap[i]
            if (queryWeight != null && documentWeight != null && queryWeight > 0.0f) {
                score += queryWeight * documentWeight
            }
        }
        return score
    }

    private fun findDocsByTermsWithPages(documents: MutableList<DocumentSnrm>, latentTerms: String, page: Pageable) {
        val result = documentSnrmRepository.findByRepresentation(latentTerms, page)
        documents += result.content
        if (result.hasNext()) {
            findDocsByTermsWithPages(documents, latentTerms, result.nextPageable())
        }
    }


    override fun save(content: String, withId: Boolean): String {
        val (externalId, passage) = extractId(content, withId)
        val documentVector = preprocess(listOf(passage))[0]
        val (latentTerms, weights) = retrieveLatentTermsAndWeights(documentVector)

        return documentSnrmRepository.save(
            DocumentSnrm(
                externalId = externalId,
                content = content,
                representation = latentTerms,
                weights = weights,
            )
        ).id ?: throwDocIdNotFoundEx()
    }

    override fun saveInBatch(contents: List<String>, withId: Boolean): List<String> = runBlocking(Dispatchers.Default) {
        log.info("Total size: ${contents.size}")

        for (chunk in contents.chunked(BATCH_SIZE_DOCUMENTS)) {
            launch {
                val idsAndDocuments = chunk.map { extractId(it, withId) }
                val ids = idsAndDocuments.map { it.first }
                val documents = idsAndDocuments.map { it.second }
                val documentVectors = preprocess(documents)
                val latentTermsAndWeightsPairs = documentVectors.map { retrieveLatentTermsAndWeights(it) }

                val entities = List(chunk.size) { idx ->
                    val latentTermsAndWeightsPair = latentTermsAndWeightsPairs[idx]
                    DocumentSnrm(
                        externalId = ids[idx],
                        content = documents[idx],
                        representation = latentTermsAndWeightsPair.first,
                        weights = latentTermsAndWeightsPair.second,
                    )
                }

                withContext(Dispatchers.IO) {
                    documentSnrmRepository.saveAll(entities)
                    log.info("Index now holds ${documentSnrmRepository.count()} documents")
                }
            }
        }

        emptyList()
    }

    override fun getFootprint(): String? {
        TODO("Not yet implemented")
    }


    private fun preprocess(contents: List<String>): Array<FloatArray> {
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

        val initArray = Array(contents.size) { FloatArray(SNRM_OUTPUT_SIZE) }

        return y.copyTo(initArray)
    }

    /**
     * For SNRM (see section 3.4 of paper) each index of the learned
     * representation is treated as a "latent term".
     * Thus, if the document representation has a dimension of 20000,
     * it is assumed that there are 20000 latent terms.
     * Therefore, if the i-th element of the document representation is non-null,
     * then the document will be added to the inverted index for latent term i.
     * The value of this element is the weight of the latent term i
     * in the learned high-dimensional latent vector space.
     *
     * @return latent terms to store in inverted index.
     */
    private fun retrieveLatentTermsAndWeights(representation: FloatArray): Pair<String, FloatArray> {
        val latentTerms = mutableListOf<Int>() // TODO: try to use StringBuilder
        val weights = mutableListOf<Float>()
        for ((index, weight) in representation.withIndex()) {
            if (weight != 0.0f) {
                latentTerms.add(index)
                weights.add(weight)
            }
        }
        return Pair(latentTerms.joinToString(" "), weights.toFloatArray())
    }
}
