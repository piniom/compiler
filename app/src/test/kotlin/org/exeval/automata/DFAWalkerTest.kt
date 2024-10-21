package org.exeval.automata

import org.exeval.automata.providers.SampleDFAProvider
import org.exeval.input.SimpleLocation
import org.exeval.input.interfaces.Location
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DFAWalkerTest {

    private var automataProvider = SampleDFAProvider()
    @Test
    fun emptyDFAWalkerTest(){
        val dfa = automataProvider.emptyIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)
        assertTrue { walker.isDead() }
        assertEquals(walker.maxAccepting, start)
        assertFalse { walker.transition('c', SimpleLocation(0, 1)) }
        assertTrue { walker.isDead() }
        assertEquals(walker.maxAccepting, start)
    }
    @Test
    fun emptyDFAWalkerComparisonTest(){
        val dfa = automataProvider.emptyIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)
        walker.transition('c', SimpleLocation(0, 1))
        assertEquals(walker.compareTo(DFAWalker(start, dfa)), 0)
    }

    @Test
    fun aStarDFAWalkerTest() {
        // Initialize the DFA using initAStarIntDFA()
        val dfa = automataProvider.aStarIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        assertFalse { walker.isDead() }
        assertEquals(walker.maxAccepting, start)

        val nextLocation = SimpleLocation(0, 1)
        assertTrue { walker.transition('a', nextLocation) }
        assertFalse { walker.isDead() }
        assertEquals(walker.maxAccepting, nextLocation)

        assertFalse { walker.transition('b', SimpleLocation(0, 2)) }

        assertFalse { walker.isDead() }
        assertEquals(walker.maxAccepting, nextLocation)
    }

    @Test
    fun aStarDFAWalkerComparisonTest() {
        val dfa = automataProvider.aStarIntDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        walker.transition('a', SimpleLocation(0, 1))

        assertEquals(walker.compareTo(DFAWalker(start, dfa)), 1)
    }

    @Test
    fun bLastAcceptingStringDFAWalkerTest() {
        // Initialize the DFA using initBLastAcceptingStringDFA()
        val dfa = automataProvider.bLastAcceptingStringDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        // Initially, the walker should not be dead and start at "kot" (not accepting)
        assertFalse { walker.isDead() }
        assertEquals(walker.maxAccepting, null)

        // Transition on 'a' from "kot" should move to "pies" and not be accepting
        val nextLocation1 = SimpleLocation(0, 1)
        assertTrue { walker.transition('a', nextLocation1) }
        assertFalse { walker.isDead() }
        assertEquals(walker.maxAccepting, null) // Still not in accepting state

        // Transition on 'b' from "pies" should move to "zolw" (accepting)
        val nextLocation2 = SimpleLocation(0, 2)
        assertTrue { walker.transition('b', nextLocation2) }
        assertFalse { walker.isDead() }
        assertEquals(walker.maxAccepting, nextLocation2) // "zolw" is accepting

        // Further transitions on 'a' or 'b' should remain valid
        assertTrue { walker.transition('a', nextLocation1) }
        assertTrue { walker.transition('b', nextLocation2) }
    }

    @Test
    fun bLastAcceptingStringDFAWalkerComparisonTest() {
        // Initialize the DFA using initBLastAcceptingStringDFA()
        val dfa = automataProvider.bLastAcceptingStringDFA
        val start = SimpleLocation(0, 0)
        val walker = DFAWalker(start, dfa)

        // Perform transitions
        walker.transition('a', SimpleLocation(0, 1))
        // walker.transition('b', SimpleLocation(0, 2))

        // Compare two walkers starting from the same location and with the same DFA
        assertEquals(walker.compareTo(DFAWalker(start, dfa)), 0)
    }
}