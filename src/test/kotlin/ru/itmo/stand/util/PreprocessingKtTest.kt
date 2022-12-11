package ru.itmo.stand.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PreprocessingKtTest {

    @Nested
    inner class CreateContexts {
        private val tokens = ("Some sentence that is used in tests " +
            "to check the correctness of the function").split(" ")

        @Test
        fun `should throw when even size is passed`() {
            val actualEx = assertThrows(IllegalArgumentException::class.java) {
                tokens.createContexts(2)
            }
            assertEquals("Size value should be odd", actualEx.message)
        }

        @Test
        fun `should throw when negative size is passed`() {
            val actualEx = assertThrows(IllegalArgumentException::class.java) {
                tokens.createContexts(-1)
            }
            assertEquals("Size value should be greater than zero", actualEx.message)
        }

        @Test
        fun `should return one window when list size is less or equal to partial window size`() {
            val actualWhenListSizeLessThanPartialWindowSize = tokens.createContexts(29)
            val actualWhenListSizeEqualToPartialWindowSize = tokens.createContexts(27)
            assertEquals(tokens, actualWhenListSizeLessThanPartialWindowSize[0])
            assertEquals(tokens, actualWhenListSizeEqualToPartialWindowSize[0])
        }

        // @Test FIXME
        // fun `should return only partial contexts`() {
        /* Try something like that:
        1. (0 until sideTokensCount)
            .takeWhile { index -> (partialWindowSize + index) <= this.size }
            .forEach { index -> result.add(this.subList(0, partialWindowSize + index)) }
        2. (this.size - size + 1 until this.size - sideTokensCount)
            .filter { it >=0 }
            .forEach { index -> result.add(this.subList(index, this.size)) }
         */
        // val contexts = tokens.createContexts(25)
        // println(contexts)
        // }

        @Test
        fun `should returns partial and full contexts`() {
            val contexts = tokens.createContexts(5)
            assertEquals(
                listOf(
                    listOf("Some", "sentence", "that"),
                    listOf("Some", "sentence", "that", "is"),
                    listOf("Some", "sentence", "that", "is", "used"),
                    listOf("sentence", "that", "is", "used", "in"),
                    listOf("that", "is", "used", "in", "tests"),
                    listOf("is", "used", "in", "tests", "to"),
                    listOf("used", "in", "tests", "to", "check"),
                    listOf("in", "tests", "to", "check", "the"),
                    listOf("tests", "to", "check", "the", "correctness"),
                    listOf("to", "check", "the", "correctness", "of"),
                    listOf("check", "the", "correctness", "of", "the"),
                    listOf("the", "correctness", "of", "the", "function"),
                    listOf("correctness", "of", "the", "function"),
                    listOf("of", "the", "function"),
                ),
                contexts,
            )
        }
    }
}
