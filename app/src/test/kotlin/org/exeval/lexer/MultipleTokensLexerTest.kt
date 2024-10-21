package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.providers.SampleDFAProvider
import org.exeval.input.StringInput
import org.exeval.utilities.StringTokenCategory
import org.exeval.utilities.interfaces.TokenCategory
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class MultipleTokensLexerTest {

    private var automataProvider = SampleDFAProvider()

    @Test
    fun multipleTokensLexerAStarAndBLastTest() {
        val aStarDFA = automataProvider.aPlusIntDFA
        val bLastDFA = automataProvider.bLastAcceptingStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            aStarDFA to StringTokenCategory("AStar"),
            bLastDFA to StringTokenCategory("BLast")
        )

        val lexer = MultipleTokensLexer(dfas)
        val input = StringInput("aaaaabbbbaaa")
        val result = lexer.run(input)
        val tokens = result.result

        assertNotNull(tokens)
        assertEquals(2, tokens.size)
        assertEquals("aaaaabbbb", tokens[0].text)
        assertEquals(setOf(StringTokenCategory("BLast")), tokens[0].categories)
        assertEquals("aaa", tokens[1].text)
        assertEquals(setOf(StringTokenCategory("AStar")), tokens[1].categories)
        assertTrue(result.diagnostics.isEmpty())
    }

    @Test
    fun multipleTokensLexerNoTokensTest() {
        val aStarDFA = automataProvider.aPlusIntDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            aStarDFA to StringTokenCategory("AStar")
        )

        val lexer = MultipleTokensLexer(dfas)
        val input = StringInput("xyz")
        val result = lexer.run(input)

        assertTrue(result.result.isEmpty())
        assertEquals(3, result.diagnostics.size)
        assertEquals("String \"x\" didn't match any tokens!", result.diagnostics[0].message)
    }

    @Test
    fun multipleTokensLexerMixedTokensTest() {
        val aPlusDFA = automataProvider.aPlusIntDFA
        val bLastDFA = automataProvider.bLastAcceptingStringDFA
        val cPlusDFA = automataProvider.cPlusIntDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            aPlusDFA to StringTokenCategory("APlus"),
            cPlusDFA to StringTokenCategory("CPlus"),
            bLastDFA to StringTokenCategory("BLast")
        )

        val lexer = MultipleTokensLexer(dfas)
        val input = StringInput("aaaaacxbbbbbabab")
        val result = lexer.run(input)
        val tokens = result.result

        assertNotNull(tokens)
        assertEquals(3, tokens.size)

        assertEquals("aaaaa", tokens[0].text)
        assertEquals(setOf(StringTokenCategory("APlus")), tokens[0].categories)

        assertEquals("c", tokens[1].text)
        assertEquals(setOf(StringTokenCategory("CPlus")), tokens[1].categories)

        assertEquals("bbbbbabab", tokens[2].text)
        assertEquals(setOf(StringTokenCategory("BLast")), tokens[2].categories)

        assertEquals(1, result.diagnostics.size)
        assertEquals("String \"x\" didn't match any tokens!", result.diagnostics[0].message)
    }

    @Test
    fun multipleTokensLexerContinuousInputTest() {
        val aStarDFA = automataProvider.aPlusIntDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            aStarDFA to StringTokenCategory("AStar")
        )

        val lexer = MultipleTokensLexer(dfas)
        val input = StringInput("aaaaaa")
        val result = lexer.run(input)
        val tokens = result.result

        assertNotNull(tokens)
        assertEquals(1, tokens.size)
        assertEquals("aaaaaa", tokens[0].text)
        assertEquals(setOf(StringTokenCategory("AStar")), tokens[0].categories)
        assertTrue(result.diagnostics.isEmpty())
    }
}