package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.lexer.interfaces.DFAMinimizer
import org.exeval.lexer.DFAmin
import org.junit.jupiter.api.Test

class DFAminTest{
    class minDFA<S> : DFA<S>{
        override val startState: S
        var acceptStates = mutableSetOf<S>()
        public var tmap = mutableMapOf<S,MutableMap<Char,S>>()
        constructor(start:S,acs:MutableSet<S>,t:MutableMap<S,MutableMap<Char,S>>){
            startState = start
            acceptStates = acs
            tmap = t
        }
        override fun isDead(state: S): Boolean{
            //a quick note: dead states will be consolidated during minimization
            return !isAccepting(state) && transitions(state).all{it.value == state}
        }
        override fun isAccepting(state: S): Boolean{
            return acceptStates.contains(state)
        }
        override fun transitions(state: S): Map<Char, S>{
            return tmap[state]!!.toMap()
        }
    }
    @Test
    fun noMergeTest(){
        var start = "A"
        var acs = mutableSetOf("D")
        var t = mutableMapOf(
            "A" to mutableMapOf('0' to "B", '1' to "C"),
            "B" to mutableMapOf('0' to "D", '1' to "D"),
            "C" to mutableMapOf('0' to "D", '1' to "D"),
            "D" to mutableMapOf('0' to "A", '1' to "A")
        )
        var minobj = DFAmin<String>()
        var base = minDFA<String>(start, acs, t)
        var min = minobj.minimize(base)
        assert(minobj.getStates(min).size == 3)
    }
}