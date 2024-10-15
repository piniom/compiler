package org.exeval.automata

import org.exeval.automata.interfaces.DFA
import io.mockk.every
import io.mockk.mockk

class SampleDFAProvider 
{
    val emptyIntDFA: DFA<Int> = initEmptyIntDFA()
    val emptyStringDFA: DFA<String> = initEmptyStringDFA()
    val aStarIntDFA: DFA<Int> = initAStarIntDFA()
    val aStarStringDFA: DFA<String> = initAStarStringDFA()
    val aLastAcceptingIntDFA: DFA<Int> = initALastAcceptingIntDFA()
    val bLastAcceptingStringDFA: DFA<String> = initBLastAcceptingStringDFA()
    val abStarStringDFA: DFA<String> = initABStarStringDFA()
    val baStarIntDFA: DFA<Int> = initBAStarStringDFA()

    private fun initEmptyIntDFA(): DFA<Int> {
        var dfa = mockk<DFA<Int>>()
        every { dfa.startState } returns 0
        every { dfa.isDead(0) } returns true
        every { dfa.isAccepting(0) } returns true
        every { dfa.transitions(0) } returns mapOf<Char, Int>()
        return dfa
    }

    private fun initEmptyStringDFA(): DFA<String> {
        var dfa = mockk<DFA<String>>()
        every { dfa.startState } returns ""
        every { dfa.isDead("") } returns true
        every { dfa.isAccepting("") } returns true
        every { dfa.transitions("") } returns mapOf<Char, String>()
        return dfa
    }

    private fun initAStarIntDFA(): DFA<Int> {
        var dfa = mockk<DFA<Int>>()
        every { dfa.startState } returns 0
        every { dfa.isDead(0) } returns false
        every { dfa.isAccepting(0) } returns true
        every { dfa.transitions(0) } returns mapOf<Char, Int>('a' to 1 )
        every { dfa.isDead(1) } returns false
        every { dfa.isAccepting(1) } returns true
        every { dfa.transitions(1) } returns mapOf<Char, Int>('a' to 1 )
        return dfa
    }

    private fun initAStarStringDFA(): DFA<String> {
        var dfa = mockk<DFA<String>>()
        every { dfa.startState } returns ""
        every { dfa.isDead("") } returns false
        every { dfa.isAccepting("") } returns true
        every { dfa.transitions("") } returns mapOf<Char, String>('a' to "ala" )
        every { dfa.isDead("ala") } returns false
        every { dfa.isAccepting("ala") } returns true
        every { dfa.transitions("ala") } returns mapOf<Char, String>('a' to "ala")
        return dfa
    }

    private fun initALastAcceptingIntDFA(): DFA<Int> {
        var dfa = mockk<DFA<Int>>()
        every { dfa.startState } returns 0
        every { dfa.isDead(0) } returns false
        every { dfa.isAccepting(0) } returns false
        every { dfa.transitions(0) } returns mapOf<Char, Int>('a' to 1, 'b' to 99 )
        every { dfa.isDead(1) } returns false
        every { dfa.isAccepting(1) } returns true
        every { dfa.transitions(1) } returns mapOf<Char, Int>('a' to 1 , 'b' to 99)
        every { dfa.isDead(99) } returns false
        every { dfa.isAccepting(99) } returns false
        every { dfa.transitions(99) } returns mapOf<Char, Int>('a' to 1 , 'b' to 99)
        return dfa
    }

    private fun initBLastAcceptingStringDFA(): DFA<String> {
        var dfa = mockk<DFA<String>>()
        every { dfa.startState } returns "kot"
        every { dfa.isDead("kot") } returns false
        every { dfa.isAccepting("kot") } returns false
        every { dfa.transitions("kot") } returns mapOf<Char, String>('a' to "pies", 
         'b' to "zolw")
        every { dfa.isDead("pies") } returns false
        every { dfa.isAccepting("pies") } returns false
        every { dfa.transitions("pies") } returns mapOf<Char, String>('a' to "pies",
         'b' to "zolw")
        every { dfa.isDead("zolw") } returns false
        every { dfa.isAccepting("zolw") } returns true
        every { dfa.transitions("zolw") } returns mapOf<Char, String>('a' to "pies",
        'b' to "zolw")
        return dfa
    }

    private fun initABStarStringDFA(): DFA<String> {
        var dfa = mockk<DFA<String>>()
        every { dfa.startState } returns "kot"
        every { dfa.isDead("kot") } returns false
        every { dfa.isAccepting("kot") } returns true
        every { dfa.transitions("kot") } returns mapOf<Char, String>('a' to "pies")
        every { dfa.isDead("pies") } returns false
        every { dfa.isAccepting("pies") } returns false
        every { dfa.transitions("pies") } returns mapOf<Char, String>('b' to "zolw")
        every { dfa.isDead("zolw") } returns false
        every { dfa.isAccepting("zolw") } returns true
        every { dfa.transitions("zolw") } returns mapOf<Char, String>('a' to "pies")
        return dfa
    }

    private fun initBAStarStringDFA(): DFA<Int> {
        var dfa = mockk<DFA<Int>>()
        every { dfa.startState } returns 0
        every { dfa.isDead(0) } returns false
        every { dfa.isAccepting(0) } returns true
        every { dfa.transitions(0) } returns mapOf<Char, Int>('b' to 99 )
        every { dfa.isDead(1) } returns false
        every { dfa.isAccepting(1) } returns true
        every { dfa.transitions(1) } returns mapOf<Char, Int>('b' to 99)
        every { dfa.isDead(99) } returns false
        every { dfa.isAccepting(99) } returns false
        every { dfa.transitions(99) } returns mapOf<Char, Int>('a' to 1)
        return dfa
    }
}