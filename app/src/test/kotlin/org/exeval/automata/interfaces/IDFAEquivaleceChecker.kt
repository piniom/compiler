package org.exeval.automata.interfaces

interface IDFAEquivaleceChecker<S> {
    fun areEquivalent(dfa1: DFA<S>, dfa2: DFA<S>): Boolean
}