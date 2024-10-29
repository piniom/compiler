package org.exeval.automata

import io.mockk.every
import io.mockk.mockk
import org.exeval.input.CommentCutter
import org.exeval.input.NotFinishedCommentException
import org.exeval.input.StringInput
import org.exeval.input.interfaces.Input
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommentCutterTests {

    private fun testWithStringInput(input: String, expected: String) {
        val commentCutter = CommentCutter(StringInput(input))

        var result = ""
        var char = commentCutter.nextChar()
        while (char != null) {
            result += char
            char = commentCutter.nextChar()
        }

        assertEquals(expected, result)
    }


    @Test
    fun withEmptyInput() {
        val emptyInput = mockk<Input>()
        every { emptyInput.nextChar() } returns null
        every { emptyInput.location } returns mockk {}

        val commentCutter = CommentCutter(emptyInput)
        val result = commentCutter.nextChar()

        assertNull(result)
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
        assertThrows(NotFinishedCommentException::class.java) { commentCutter.nextChar() }
    }
}
