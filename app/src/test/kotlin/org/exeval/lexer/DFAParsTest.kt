
package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.interfaces.NFA
import org.exeval.lexer.interfaces.DFAParser
import org.exeval.lexer.DFAPars
import org.junit.jupiter.api.Test

class DFAParsTest{
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
    fun NFAToDFATest(){
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
        var parsedDFA = DFAPars().parse(nfa)
        var state = parsedDFA.startState
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
        for (i in 1..5) {
            state2 = parsedDFA.transitions(state2)['b']!!
            assert(parsedDFA.isAccepting(state2))
            state2 = parsedDFA.transitions(state2)['c']!!
            assert(parsedDFA.isAccepting(state2))
        }
        assert(parsedDFA.isDead(parsedDFA.transitions(state2)['a']!!))
        assert(parsedDFA.isDead(parsedDFA.transitions(state2)['z']!!))
    }
}