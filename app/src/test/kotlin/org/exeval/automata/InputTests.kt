package org.exeval.automata

import io.mockk.every
import io.mockk.mockk
import org.exeval.input.StringInput
import org.exeval.input.FileInput
import org.exeval.input.interfaces.Input
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InputTests {
    private fun stringInputTest(input: String, expected: String) {
        val inputClass = StringInput(input)

        for (c: Char in expected) {
            var v = inputClass.nextChar()
            if (v != c) {
                assertFalse(false, "Badly matched input")
            }
        }

        assertTrue(true, "Correctly matched input")
    }

    @Test
    fun instructionsTest() {
        val input = "a,b,c"
        val output = "abc"
        stringInputTest(input, output)
    }

    @Test
    fun whitespacesTest() {
        val input = "a, b , c"
        val output = "abc"
        stringInputTest(input, output)
    }

    @Test
    fun multicharTest() {
        val input = "aaa, bbb, cdf"
        val output = "aaabbbcdf"
        stringInputTest(input, output)
    }

    @Test
    fun commasTest() {
        val input = ",,,"
        val output = ""
        stringInputTest(input, output)
    }
}
