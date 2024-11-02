package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.providers.SampleDFAProvider
import org.exeval.input.SimpleLocation
import org.exeval.input.StringInput
import org.exeval.utilities.StringTokenCategory
import org.exeval.utilities.diagnostics.TextDidNotMatchAnyTokensDiagnostics
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.TokenCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNull

class SingleTokenLexerTest {

    private var automataProvider = SampleDFAProvider()

    @Test
    fun singleTokenLexerAStarDFATest1() {
        val dfa = automataProvider.aStarIntDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(dfa to StringTokenCategory("AStar"))

        val lexer = SingleTokenLexer(dfas, StringInput("aaaaabaaa"))

        val result = lexer.run()
        val token = result.result

        assertEquals(result.diagnostics, listOf<Diagnostics>())
        assertNotNull(token)
        assertEquals("aaaaa", token?.text)
        assertEquals(setOf(StringTokenCategory("AStar")), token?.categories)
    }

    @Test
    fun singleTokenLexerAStarDFATest2() {
        val aStarDFA = automataProvider.aStarStringDFA
        val bLastDFA = automataProvider.bLastAcceptingStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            aStarDFA to StringTokenCategory("AStar"),
            bLastDFA to StringTokenCategory("BLast")
        )

        val lexer = SingleTokenLexer(dfas, StringInput("aaa"))

        val result = lexer.run()
        val token = result.result

        assertEquals(result.diagnostics, listOf<Diagnostics>())
        assertNotNull(token)
        assertEquals("aaa", token?.text)
        assertEquals(setOf(StringTokenCategory("AStar")), token?.categories)
    }

    @Test
    fun singleTokenLexerBLastAcceptingStringDFATest() {
        val dfa = automataProvider.bLastAcceptingStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(dfa to StringTokenCategory("BLast"))

        val lexer = SingleTokenLexer(dfas, StringInput("ababbbbas"))

        val result = lexer.run()
        val token = result.result

        assertNotNull(token)
        assertEquals("ababbbb", token?.text)
        assertEquals(setOf(StringTokenCategory("BLast")), token?.categories)
    }

    @Test
    fun singleTokenLexerABStarStringDFATest() {
        val dfa = automataProvider.abStarStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(dfa to StringTokenCategory("ABStar"))

        val lexer = SingleTokenLexer(dfas, StringInput("abababbbba"))

        val result = lexer.run()
        val token = result.result

        assertNotNull(token)
        assertEquals("ababab", token?.text)
        assertEquals(setOf(StringTokenCategory("ABStar")), token?.categories)
    }

    @Test
    fun singleTokenLexerMultipleCategoriesTest() {
        val dfa1 = automataProvider.aStarStringDFA
        val dfa2 = automataProvider.abStarStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            dfa1 to StringTokenCategory("AStar"),
            dfa2 to StringTokenCategory("ABStar")
        )

        val lexer = SingleTokenLexer(dfas, StringInput("aaaaaabbbbb"))

        val result = lexer.run()
        val token = result.result

        assertNotNull(token)
        assertEquals("aaaaaa", token?.text)
        assertEquals(setOf(StringTokenCategory("AStar")), token?.categories)
    }

    @Test
    fun singleTokenLexerBothAcceptingOneLongerCategoriesTest() {
        val dfa1 = automataProvider.aLastAcceptingIntDFA
        val dfa2 = automataProvider.bLastAcceptingStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            dfa1 to StringTokenCategory("ALast"),
            dfa2 to StringTokenCategory("BLast")
        )

        val lexer = SingleTokenLexer(dfas, StringInput("aabbb"))

        val result = lexer.run()
        val token = result.result

        assertNotNull(token)
        assertEquals("aabbb", token?.text)
        assertEquals(setOf(StringTokenCategory("BLast")), token?.categories)
    }

    @Test
    fun singleTokenLexerBothAcceptingSameLengthCategoriesTest() {
        val dfa1 = automataProvider.aStarIntDFA
        val dfa2 = automataProvider.aStarStringDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(
            dfa1 to StringTokenCategory("ALast1"),
            dfa2 to StringTokenCategory("ALast2")
        )

        val input = StringInput("aabbb")
        val lexer = SingleTokenLexer(dfas, input)

        val result = lexer.run()
        val token = result.result

        assertNotNull(token)
        assertEquals("aa", token?.text)
        assertEquals(setOf(StringTokenCategory("ALast1"), StringTokenCategory("ALast2")), token?.categories)
        assertEquals(SimpleLocation(0, 2), input.location )
    }

    @Test
    fun singleTokenLexerNoMatchTest() {
        val dfa = automataProvider.aLastAcceptingIntDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(dfa to StringTokenCategory("AStar"))

        val lexer = SingleTokenLexer(dfas, StringInput("bbb"))

        val result = lexer.run()
        val token = result.result

        val start = SimpleLocation(0, 0)

        assertNull(token)
        assertEquals(
            listOf(
                TextDidNotMatchAnyTokensDiagnostics.create("bbb",
                    // This is what i get from the StringInput class, it's probably wront
                    SimpleLocation(0, 0),
                    SimpleLocation(1, 0)
                )
            ),
            result.diagnostics
        )
    }
}
