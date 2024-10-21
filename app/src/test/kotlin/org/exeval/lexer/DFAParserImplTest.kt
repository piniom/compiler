
package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.interfaces.NFA
import org.exeval.lexer.interfaces.DFAParser
import org.exeval.lexer.DFAParserImpl
import org.junit.jupiter.api.Test

class DFAParserImplTest{
    class SimpleNFA<S> : NFA<S>{
        override val startState: S
        override val acceptingState: S
        var stateTransitions: Map<S, Map<Char, S>>
        var stateETransitions: Map<S, Set<S>>

        constructor(start: S, accepting: S, trans: Map<S, Map<Char, S>>, eTrans: Map<S, Set<S>>) {
            startState = start
            acceptingState = accepting
            stateTransitions = trans
            stateETransitions = eTrans
        }
    
        override fun transitions(state: S): Map<Char, S> {
            return stateTransitions[state] ?: run {
                mapOf<Char, S>()
            }
        }
        override fun eTransitions(state: S): Set<S> {
            return stateETransitions[state] ?: run {
                setOf<S>()
            }
        }
    }

    @Test
    fun SimpleNFAToDFATest() {
        var start = 0
        var accepting = 9
        var t = mapOf(
            0 to mapOf('a' to 1),
            4 to mapOf('b' to 5),
            6 to mapOf('c' to 7)
        )
        var et = mapOf(
            1 to setOf(2),
            2 to setOf(3, 9),
            3 to setOf(4, 6),
            5 to setOf(8),
            7 to setOf(8),
            8 to setOf(3, 9)
        )
        // NFA for "a(b | c)*"
        var nfa = SimpleNFA(start, accepting, t, et)
        var parsedDFA = DFAParserImpl().parse(nfa)
        var state = parsedDFA.startState
        assert(!parsedDFA.isAccepting(state))
        assert(!parsedDFA.isDead(state))
        state = parsedDFA.transitions(state)['a']!!
        assert(parsedDFA.isAccepting(state))
        var state2 = parsedDFA.transitions(state)['b']!!
        assert(parsedDFA.isAccepting(state2))
        var state3 = parsedDFA.transitions(state)['c']!!
        assert(parsedDFA.isAccepting(state3))
        state = parsedDFA.startState
        state = parsedDFA.transitions(state)['b']!!
        assert(!parsedDFA.isAccepting(state))
        assert(parsedDFA.isDead(state))
        state2 = parsedDFA.transitions(state2)['b']!!
        assert(parsedDFA.isAccepting(state2))
        state2 = parsedDFA.transitions(state2)['c']!!
        assert(parsedDFA.isAccepting(state2))
        assert(parsedDFA.isDead(parsedDFA.transitions(state2)['a']!!))
        assert(parsedDFA.isDead(parsedDFA.transitions(state2)['z']!!))
    }

    @Test
    fun EmptyStringTwoStates() {
        var start = 0
        var accepting = 1
        var t = mapOf<Int, Map<Char, Int>>()
        var et = mapOf(
            0 to setOf(1)
        )
        var nfa = SimpleNFA(start, accepting, t, et)
        var parsedDFA = DFAParserImpl().parse(nfa)
        var state = parsedDFA.startState
        assert(parsedDFA.isAccepting(state))
        state = parsedDFA.transitions(state)['a']!!
        assert(parsedDFA.isDead(state))
    }

    @Test
    fun EmptyStringOneState() {
        var start = 0
        var accepting = 0
        var t = mapOf<Int, Map<Char, Int>>()
        var et = mapOf<Int, Set<Int>>()
        var nfa = SimpleNFA(start, accepting, t, et)
        var parsedDFA = DFAParserImpl().parse(nfa)
        var state = parsedDFA.startState
        assert(parsedDFA.isAccepting(state))
        state = parsedDFA.transitions(state)['a']!!
        assert(parsedDFA.isDead(state))
    }

    @Test
    fun ComeBackToAcceptingState() {
        var start = 0
        var accepting = 2
        var t = mapOf<Int, Map<Char, Int>>(
            0 to mapOf('a' to 1),
            1 to mapOf(
                'a' to 0,
                'b' to 2
            )
        )
        var et = mapOf<Int, Set<Int>>(
            0 to setOf(2)
        )
        var nfa = SimpleNFA(start, accepting, t, et)
        var parsedDFA = DFAParserImpl().parse(nfa)
        var state = parsedDFA.startState
        assert(parsedDFA.isAccepting(state))
        state = parsedDFA.transitions(state)['a']!!
        assert(!parsedDFA.isDead(state))
        state = parsedDFA.transitions(state)['a']!!
        assert(parsedDFA.isAccepting(state))
        state = parsedDFA.transitions(state)['a']!!
        state = parsedDFA.transitions(state)['b']!!
        assert(parsedDFA.isAccepting(state))
    }

    @Test
    fun AAorBB() {
        var start = 0
        var accepting = 7
        var t = mapOf<Int, Map<Char, Int>>(
            1 to mapOf('A' to 2),
            2 to mapOf('A' to 3),
            4 to mapOf('B' to 5),
            5 to mapOf('B' to 6)
        )
        var et = mapOf<Int, Set<Int>>(
            0 to setOf(1, 4),
            3 to setOf(7),
            6 to setOf(7)
        )
        var nfa = SimpleNFA(start, accepting, t, et)
        var parsedDFA = DFAParserImpl().parse(nfa)
        // AA
        var state = parsedDFA.startState
        assert(!parsedDFA.isAccepting(state))
        assert(!parsedDFA.isDead(state))
        state = parsedDFA.transitions(state)['A']!!
        assert(!parsedDFA.isAccepting(state))
        assert(!parsedDFA.isDead(state))
        state = parsedDFA.transitions(state)['A']!!
        assert(parsedDFA.isAccepting(state))
        assert(!parsedDFA.isDead(state))
        // BB
        state = parsedDFA.startState
        state = parsedDFA.transitions(state)['B']!!
        assert(!parsedDFA.isAccepting(state))
        assert(!parsedDFA.isDead(state))
        state = parsedDFA.transitions(state)['B']!!
        assert(parsedDFA.isAccepting(state))
        assert(!parsedDFA.isDead(state))
    }

}