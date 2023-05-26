package ru.itmo.stand.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PreprocessingKtTest {

    @Nested
    inner class CreateWindows {
        private val tokens = "Some sentence that is used in tests to check the correctness of the function"
            .split(" ")

        @Test
        fun `should throw when even size is passed`() {
            val actualEx = assertThrows<IllegalArgumentException> {
                tokens.createWindows(2)
            }
            assertEquals("Size value should be odd", actualEx.message)
        }

        @Test
        fun `should throw when negative size is passed`() {
            val actualEx = assertThrows<IllegalArgumentException> {
                tokens.createWindows(-1)
            }
            assertEquals("Size value should be greater than zero", actualEx.message)
        }

        @Test
        fun `should return one window when list size is less or equal to partial window size`() {
            val actualWhenListSizeLessThanPartialWindowSize = tokens.createWindows(29)
            val actualWhenListSizeEqualToPartialWindowSize = tokens.createWindows(27)
            assertEquals(Window(middleTokenIndex = 0, content = tokens), actualWhenListSizeLessThanPartialWindowSize[0])
            assertEquals(Window(middleTokenIndex = 0, content = tokens), actualWhenListSizeEqualToPartialWindowSize[0])
        }

        // @Test FIXME
        // fun `should return only partial windows`() {
        /* Try something like that:
        1. (0 until sideTokensCount)
            .takeWhile { index -> (partialWindowSize + index) <= this.size }
            .forEach { index -> result.add(this.subList(0, partialWindowSize + index)) }
        2. (this.size - size + 1 until this.size - sideTokensCount)
            .filter { it >=0 }
            .forEach { index -> result.add(this.subList(index, this.size)) }
         */
        // val windows = tokens.createWindows(25)
        // println(windows)
        // }

        @Test
        fun `should returns partial and full windows`() {
            val windows = tokens.createWindows(5)
            assertEquals(
                listOf(
                    Window("Some", 0, listOf("Some", "sentence", "that")),
                    Window("sentence", 1, listOf("Some", "sentence", "that", "is")),
                    Window("that", 2, listOf("Some", "sentence", "that", "is", "used")),
                    Window("is", 2, listOf("sentence", "that", "is", "used", "in")),
                    Window("used", 2, listOf("that", "is", "used", "in", "tests")),
                    Window("in", 2, listOf("is", "used", "in", "tests", "to")),
                    Window("tests", 2, listOf("used", "in", "tests", "to", "check")),
                    Window("to", 2, listOf("in", "tests", "to", "check", "the")),
                    Window("check", 2, listOf("tests", "to", "check", "the", "correctness")),
                    Window("the", 2, listOf("to", "check", "the", "correctness", "of")),
                    Window("correctness", 2, listOf("check", "the", "correctness", "of", "the")),
                    Window("of", 2, listOf("the", "correctness", "of", "the", "function")),
                    Window("the", 2, listOf("correctness", "of", "the", "function")),
                    Window("function", 2, listOf("of", "the", "function")),
                ),
                windows,
            )
        }
    }
}
