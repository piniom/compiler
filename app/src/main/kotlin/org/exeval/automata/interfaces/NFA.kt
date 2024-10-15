package org.exeval.automata.interfaces

interface NFA<S> {
    val startState: S
    val acceptingState: S

    fun transitions(state: S): Map<Char, S>
    fun eTransitions(state: S): Set<S>
}