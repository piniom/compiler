package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.lexer.interfaces.DFAMinimizer
import org.exeval.lexer.DFAmin

class DFAminTest{
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
        var base = DFAmin.minDFA<String>(start, acs, t)
        var min = minobj.minimize(base)
        assert(minobj.getStates(min).size == 3)
    }
}