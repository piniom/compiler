package org.exeval.automata

import io.mockk.every
import io.mockk.mockk
import org.exeval.input.StringInput
import org.exeval.input.FileInput
import org.exeval.input.interfaces.Input
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString

class InputTests {
    private fun stringInputTest(input: String, expected: String) {
        val inputClass = StringInput(input)

        for (c: Char in expected) {
            var v = inputClass.nextChar()
            if (v != c) {
                assertFalse(false, "Badly matched input")
            }
        }

        assertTrue(true, "Correctly matched")
    }

    private fun fileInputTest(filename: String, expected: String) {
        val inputClass = FileInput(filename)

        for (c: Char in expected) {
            var v = inputClass.nextChar()
            if (v != c) {
                assertFalse(false, "Badly matched input")
            }
        }

        assertTrue(true, "Correctly matched")
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

    @Test
    fun emptyLinesTest() {
        val input = "  , ,,"
        val output = ""
        stringInputTest(input, output)
    }

    @Test
    fun emptyFileTest() {
        val filename = Path("src/test/kotlin/org/exeval/automata/fileinput/empty.txt").absolute()
        val output = ""
        fileInputTest(filename.toString(), output)
    }

    @Test
    fun fileInstructionTest() {
        val filename = Path("src/test/kotlin/org/exeval/automata/fileinput/instructions.txt").absolute()
        val output = ""
        fileInputTest(filename.toString(), output)
    }

    @Test
    fun fileWhitespacesTest() {
        val filename = Path("src/test/kotlin/org/exeval/automata/fileinput/whitespaces.txt").absolute()
        val output = ""
        fileInputTest(filename.toString(), output)
    }

    @Test
    fun fileMulticharTest() {
        val filename = Path("src/test/kotlin/org/exeval/automata/fileinput/multichar.txt").absolute()
        val output = ""
        fileInputTest(filename.toString(), output)
    }


    @Test
    fun fileCommasTest() {
        val filename = Path("src/test/kotlin/org/exeval/automata/fileinput/commas.txt").absolute()
        val output = ""
        fileInputTest(filename.toString(), output)
    }
}
