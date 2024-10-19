package org.exeval.automata

import io.mockk.every
import io.mockk.mockk
import org.exeval.input.CommentCutter
import org.exeval.input.StringInput
import org.exeval.input.interfaces.Input
import org.exeval.utilities.interfaces.OperationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommentCutterTests {

    private fun testWithStringInput(input: String, expected: String) {
        val commentCutter = CommentCutter(StringInput(input))

        fun getNextResult(): Char? {
            val result = commentCutter.nextChar()
            assertTrue(result.diagnostics.isEmpty())
            return result.result
        }

        var result = ""
        var char = getNextResult()
        while (char != null) {
            result += char
            char = getNextResult()
        }

        assertEquals(expected, result)
    }


    @Test
    fun withEmptyInput() {
        val emptyInput = mockk<Input>()
        every { emptyInput.nextChar() } returns OperationResult(null, emptyList())
        every { emptyInput.location } returns mockk {}

        val commentCutter = CommentCutter(emptyInput)
        val result = commentCutter.nextChar()

        assertNull(result.result)
        assertTrue(result.diagnostics.isEmpty())
    }

    @Test
    fun withLineWithoutComment() {
        val testText = "aaa bbb ccc"
        testWithStringInput(testText, testText)
    }

    @Test
    fun withSimpleMultiLineComment() {
        val testText = "aaa /*bbb*/ ccc"
        val expected = "aaa   ccc"
        testWithStringInput(testText, expected)
    }

    @Test
    fun withSimpleSingleLineComment() {
        val testText = "aaa bbb //ccc"
        val expected = "aaa bbb "
        testWithStringInput(testText, expected)
    }

    @Test
    fun withSimpleOnlyFirstCharOfComment() {
        val testText = "aaa bbb / ccc"
        testWithStringInput(testText, testText)
    }

    @Test
    fun withMultiLineTextWithSingleLineComment() {
        val testText = """
            aaaa // b
            // xxx
            abd
            xd // xd
            aa
        """.trimIndent()

        val expected = """
            aaaa 
            
            abd
            xd 
            aa
        """.trimIndent()

        testWithStringInput(testText, expected)
    }

    @Test
    fun withMultiLineTextWithMultiLineComment() {
        val testText = """
            aaaa /* b
            xxx
            ab*/d
            xd /*xd
            a*/a
        """.trimIndent()

        val expected = """
            aaaa 
            d
            xd 
            a
        """.trimIndent()

        testWithStringInput(testText, expected)
    }

    @Test
    fun withNestedMultiLineComment() {
        val testText = """
            aaa /* a /* 
            /**/ /*x*/
            */ a */bb
        """.trimIndent()

        val expected = """
            aaa 
            bb
        """.trimIndent()

        testWithStringInput(testText, expected)
    }

    @Test
    fun withNotFinished() {
        val commentCutter = CommentCutter(StringInput("/*"))
        val result = commentCutter.nextChar()

        val expectedDiagnosticLength = 1

        assertNull(result.result)
        assertEquals(expectedDiagnosticLength,  result.diagnostics.size)
        assertEquals(result.diagnostics.first().message, CommentCutter.NOT_FINISHED_COMMENT_ERROR_MESSAGE)
    }
}
