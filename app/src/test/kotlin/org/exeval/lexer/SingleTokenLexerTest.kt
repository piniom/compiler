package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.providers.SampleDFAProvider
import org.exeval.input.StringInput
import org.exeval.utilities.StringTokenCategory
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.TokenCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertNull

class SingleTokenLexerTest {

    private var automataProvider = SampleDFAProvider()

    @Test
    fun singleTokenLexerAStarDFATest() {
        val dfa = automataProvider.aStarIntDFA
        val dfas: Map<DFA<*>, TokenCategory> = mapOf(dfa to StringTokenCategory("AStar"))

        val lexer = SingleTokenLexer(dfas, StringInput("aaaaabaaa"))

        val result = lexer.run()
        val token = result.result

        assertEquals(result.diagnostics, listOf<Diagnostics>())
        assertNotNull(token)
        assertEquals("aaaaa", token?.text )
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
}