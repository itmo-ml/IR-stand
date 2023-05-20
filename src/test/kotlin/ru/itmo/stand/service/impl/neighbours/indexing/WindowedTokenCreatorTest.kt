package ru.itmo.stand.service.impl.neighbours.indexing

import io.mockk.every
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.itmo.stand.fixtures.preprocessingPipelineExecutor
import ru.itmo.stand.fixtures.standProperties
import ru.itmo.stand.service.model.Document
import ru.itmo.stand.util.createPath
import java.io.File

class WindowedTokenCreatorTest {

    private val windowedTokenCreator = WindowedTokenCreator(
        preprocessingPipelineExecutor(),
        standProperties(),
    )

    @Test
    fun `should create windowed tokens`() {
        val content = "Definition of Central nervous system (CNS): " +
            "The central nervous system is that part of the nervous system that consists " +
            "of the brain and spinal cord."
        val windows = arrayOf(
            "0|definition central nervous",
            "1|definition central nervous system",
            "2|cn ##s central nervous system",
            "2|definition central nervous system cn",
            "2|##s central nervous system part",
            "2|system part nervous system consists",
            "2|central nervous system cn ##s",
            "2|central nervous system part nervous",
            "2|part nervous system consists brain",
            "2|nervous system cn ##s central",
            "2|system cn ##s central nervous",
            "2|nervous system part nervous system",
            "2|nervous system consists brain spinal",
            "2|system consists brain spinal cord",
            "2|consists brain spinal cord",
            "2|brain spinal cord",
        )

        mockkStatic(File::createPath) {
            every { any<File>().createPath() } returns File.createTempFile("windowed-token-creator-test", null)
            val windowedTokensFile = windowedTokenCreator.create(sequenceOf(Document("id", content)))

            val result = windowedTokensFile
                .bufferedReader()
                .readLines()
                .map { line ->
                    val (token, windowsString) = line.split(WindowedTokenCreator.TOKEN_WINDOWS_SEPARATOR)
                    val docIdsByWindowPairs: Set<Pair<String, List<String>>> = windowsString
                        .split(WindowedTokenCreator.WINDOWS_SEPARATOR)
                        .filter { it.isNotBlank() }
                        .mapTo(HashSet()) {
                            val (window, docIdsString) = it.split(WindowedTokenCreator.WINDOW_DOC_IDS_SEPARATOR)
                            val docIds = docIdsString.split(WindowedTokenCreator.DOC_IDS_SEPARATOR)
                            window to docIds
                        }
                    token to docIdsByWindowPairs
                }

            assertThat(windowedTokensFile.name).startsWith("windowed-token-creator-test")
            assertThat(result).containsExactlyInAnyOrder(
                "definition" to setOf(windows[0] to listOf("id")),
                "central" to setOf(windows[1] to listOf("id"), windows[2] to listOf("id")),
                "nervous" to setOf(windows[3] to listOf("id"), windows[4] to listOf("id"), windows[5] to listOf("id")),
                "system" to setOf(windows[6] to listOf("id"), windows[7] to listOf("id"), windows[8] to listOf("id")),
                "cn" to setOf(windows[9] to listOf("id")),
                "##s" to setOf(windows[10] to listOf("id")),
                "part" to setOf(windows[11] to listOf("id")),
                "consists" to setOf(windows[12] to listOf("id")),
                "brain" to setOf(windows[13] to listOf("id")),
                "spinal" to setOf(windows[14] to listOf("id")),
                "cord" to setOf(windows[15] to listOf("id")),
            )
        }
    }
}
