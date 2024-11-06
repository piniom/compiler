package org.exeval.automata.interfaces

interface DFAEquivalenceChecker {
    fun <S, T>areEquivalent(dfa1: DFA<S>, dfa2: DFA<T>): Boolean
}