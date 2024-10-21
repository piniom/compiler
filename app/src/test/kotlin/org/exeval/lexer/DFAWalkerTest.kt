package org.exeval.lexer

import org.exeval.automata.providers.SampleDFAProvider
import org.exeval.input.SimpleLocation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class DFAWalkerTest {

    private var automataProvider = SampleDFAProvider()

    @Test
    fun emptyDFAWalkerTest() {
        val dfa = automataProvider.emptyIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)
        assertTrue { walker.isDead() }
        assertEquals(start, walker.maxAccepting)
        assertEquals(0, walker.maxAcceptingCount)
        assertFalse { walker.transition('c', SimpleLocation(0, 1)) }
        assertTrue { walker.isDead() }
        assertEquals(start, walker.maxAccepting)
        assertEquals(0, walker.maxAcceptingCount)
    }

    @Test
    fun emptyDFAWalkerComparisonTest() {
        val dfa = automataProvider.emptyIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)
        walker.transition('c', SimpleLocation(0, 1))
        assertEquals(0, walker.compareTo(DFAWalker(start, dfa)))
    }

    @Test
    fun aStarDFAWalkerTest() {
        val dfa = automataProvider.aStarIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        assertFalse { walker.isDead() }
        assertEquals(start, walker.maxAccepting)

        val nextLocation = SimpleLocation(0, 1)
        assertTrue { walker.transition('a', nextLocation) }
        assertFalse { walker.isDead() }
        assertEquals(nextLocation, walker.maxAccepting)

        assertFalse { walker.transition('b', SimpleLocation(0, 2)) }

        assertFalse { walker.isDead() }
        assertEquals(nextLocation, walker.maxAccepting)
    }

    @Test
    fun aStarDFAWalkerComparisonTest() {
        val dfa = automataProvider.aStarIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        walker.transition('a', SimpleLocation(0, 1))

        assertEquals(1, walker.compareTo(DFAWalker(start, dfa)))
    }

    @Test
    fun bLastAcceptingStringDFAWalkerTest() {
        val dfa = automataProvider.bLastAcceptingStringDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        assertFalse { walker.isDead() }
        assertEquals(null, walker.maxAccepting)

        val nextLocation1 = SimpleLocation(0, 1)
        assertTrue { walker.transition('a', nextLocation1) }
        assertFalse { walker.isDead() }
        assertEquals(null, walker.maxAccepting)

        val nextLocation2 = SimpleLocation(0, 2)
        assertTrue { walker.transition('b', nextLocation2) }
        assertFalse { walker.isDead() }
        assertEquals(nextLocation2, walker.maxAccepting)

        assertTrue { walker.transition('a', nextLocation1) }
        assertTrue { walker.transition('b', nextLocation2) }
    }

    @Test
    fun bLastAcceptingStringDFAWalkerComparisonTest() {
        val dfa = automataProvider.bLastAcceptingStringDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        walker.transition('a', SimpleLocation(0, 1))

        assertEquals(0, walker.compareTo(DFAWalker(start, dfa)))
    }
}