package ru.itmo.stand.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConcurrencyKtTest {

    @Test
    fun `should distribute tasks between two workers independently`() = runTest {
        processConcurrently(flowOf(10, 2, 2, 2, 2, 2, 10, 2, 2, 2, 2, 2), 2, {}) { value ->
            delay(value.toLong())
        }

        assertThat(currentTime).isEqualTo(20)
    }

    @Test
    fun `should distribute tasks between three workers independently`() = runTest {
        processConcurrently(flowOf(10, 2, 2, 2, 2, 2, 10, 2, 2, 2, 2, 2), 3, {}) { value ->
            delay(value.toLong())
        }

        assertThat(currentTime).isEqualTo(14)
    }

    @Test
    fun `should distribute tasks between four workers independently`() = runTest {
        processConcurrently(flowOf(10, 2, 2, 2, 2, 2, 10, 2, 2, 2, 2, 2), 4, {}) { value ->
            delay(value.toLong())
        }

        assertThat(currentTime).isEqualTo(12)
    }
}
