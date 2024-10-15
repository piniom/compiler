package org.exeval.automata.interfaces

interface DFAEquivaleceChecker<S, T> {
    fun areEquivalent(dfa1: DFA<S>, dfa2: DFA<T>): Boolean
}