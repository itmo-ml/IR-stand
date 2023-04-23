package ru.itmo.stand.service.impl

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.tensorflow.SavedModelBundle
import org.tensorflow.Tensor
import ru.itmo.stand.config.Method
import ru.itmo.stand.config.Params.BATCH_SIZE_DOCUMENTS
import ru.itmo.stand.config.Params.MAX_DOC_LEN
import ru.itmo.stand.config.Params.MAX_QUERY_LEN
import ru.itmo.stand.config.Params.SNRM_OUTPUT_SIZE
import ru.itmo.stand.config.StandProperties
import ru.itmo.stand.service.DocumentService
import ru.itmo.stand.service.footprint.IndexFootprintFinder
import ru.itmo.stand.service.model.Format
import ru.itmo.stand.service.preprocessing.StopWordRemover
import ru.itmo.stand.storage.lucene.model.ContentSnrm
import ru.itmo.stand.storage.lucene.model.DocumentSnrm
import ru.itmo.stand.storage.lucene.repository.ContentSnrmRepository
import ru.itmo.stand.storage.lucene.repository.DocumentSnrmRepository
import ru.itmo.stand.util.extractId
import ru.itmo.stand.util.lineSequence
import ru.itmo.stand.util.throwDocIdNotFoundEx
import ru.itmo.stand.util.toTokens
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class DocumentSnrmService(
    private val documentSnrmRepository: DocumentSnrmRepository,
    private val contentSnrmRepository: ContentSnrmRepository,
    private val indexFootprintFinder: IndexFootprintFinder,
    private val stopWordRemover: StopWordRemover,
    private val stanfordCoreNlp: StanfordCoreNLP,
    private val standProperties: StandProperties,
) : DocumentService {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val model by lazy {
        val basePath = standProperties.app.basePath
        runCatching { SavedModelBundle.load("$basePath/models/snrm/frozen", "serve") }
            .onSuccess { log.info("SNRM model is loaded") }
            .onFailure { log.error("Could not load SNRM model", it) }
            .getOrThrow()
    }
    private val termToId by lazy {
        mutableMapOf("UNKNOWN" to 0).also {
            var id = 1
            Files.lines(Paths.get("src/main/resources/data/tokens.txt")).forEach { term -> it[term] = id++ }
        }.toMap()
    }

    override val method: Method
        get() = Method.SNRM

    override fun find(id: String): String? = contentSnrmRepository.findByIndexId(id)?.content

    override fun search(queries: File, format: Format): List<String> {
        val queryVector = preprocess(listOf(queries.readLines().single()), PreprocessingType.QUERY)[0]
        val (latentTerms, weights) = retrieveLatentTermsAndWeights(queryVector)
        val documents = documentSnrmRepository.findAllByRepresentation(latentTerms)

        val queryLatentTermWeightMap = convertToMap(latentTerms, weights)
        // TODO: think about improving the algorithm
        return documents.map {
            Pair(
                it.id ?: throwDocIdNotFoundEx(),
                dotProduct(queryLatentTermWeightMap, convertToMap(it.representation, it.weights)),
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

    private fun dotProduct(
        queryLatentTermWeightMap: Map<Int, Float>,
        documentLatentTermWeightMap: Map<Int, Float>,
    ): Double {
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

    override fun save(content: String, withId: Boolean): String {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")
        val (externalId, passage) = extractId(content)
        val documentVector = preprocess(listOf(passage), PreprocessingType.DOCUMENT)[0]
        val (latentTerms, weights) = retrieveLatentTermsAndWeights(documentVector)

        val documentSnrm = documentSnrmRepository.save(
            DocumentSnrm(
                externalId = externalId.toLong(),
                representation = latentTerms,
                weights = weights,
            ),
        )
        val id = documentSnrm.id ?: throwDocIdNotFoundEx()
        contentSnrmRepository.save(ContentSnrm(indexId = id, content = content))
        return id
    }

    override fun saveInBatch(contents: File, withId: Boolean): List<String> = runBlocking(Dispatchers.Default) {
        if (!withId) throw UnsupportedOperationException("Save without id is not supported")

        for (chunk in contents.lineSequence().chunked(BATCH_SIZE_DOCUMENTS)) {
            launch {
                val idsAndDocuments = chunk.map { extractId(it) }
                val ids = idsAndDocuments.map { it.id }
                val documents = idsAndDocuments.map { it.content }
                val documentVectors = preprocess(documents, PreprocessingType.DOCUMENT)
                val latentTermsAndWeightsPairs = documentVectors.map { retrieveLatentTermsAndWeights(it) }

                val entitiesAndContents = List(chunk.size) { idx ->
                    val latentTermsAndWeightsPair = latentTermsAndWeightsPairs[idx]
                    Pair(
                        DocumentSnrm(
                            externalId = ids[idx].toLong(),
                            representation = latentTermsAndWeightsPair.first,
                            weights = latentTermsAndWeightsPair.second,
                        ),
                        documents[idx],
                    )
                }

                withContext(Dispatchers.IO) {
                    val entities = entitiesAndContents.map { it.first }
                    val docContents = entitiesAndContents.map { it.second }
                    val savedEntities = documentSnrmRepository.saveAll(entities)
                    contentSnrmRepository.saveAll(
                        savedEntities.mapIndexed { idx, it ->
                            ContentSnrm(
                                indexId = it.id!!,
                                content = docContents[idx],
                            )
                        },
                    )
                }
            }
        }

        emptyList()
    }

    override fun getFootprint(): String = indexFootprintFinder.findFootprint(method.indexName)

    enum class PreprocessingType(val feedOperation: String, val fetchOperation: String, val maxInputArrayLength: Int) {
        QUERY("Placeholder_5", "Mean_6", MAX_QUERY_LEN),
        DOCUMENT("Placeholder_4", "Mean_5", MAX_DOC_LEN),
    }

    private fun preprocess(contents: List<String>, type: PreprocessingType): Array<FloatArray> {
        // create session
        val sess = model.session()

        // tokenization
        val tokensList: List<List<String>> = contents.map { it.toTokens(stanfordCoreNlp) }

        // form term id list
        val termIds: List<MutableList<Int>> = tokensList.map { tokens ->
            val temp = stopWordRemover.preprocess(tokens)
                .map { if (termToId.containsKey(it)) termToId[it]!! else termToId["UNKNOWN"]!! }
                .toMutableList()
            // fill until max doc length or trim for it
            for (i in 1..(type.maxInputArrayLength - temp.size)) temp.add(0)
            temp.subList(0, type.maxInputArrayLength)
        }

        // create tensor
        val x = Tensor.create(termIds.map { it.toIntArray() }.toTypedArray())

        // inference
        val y = sess.runner()
            .feed(type.feedOperation, x)
            .fetch(type.fetchOperation)
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
