package org.example.automata

interface INFA<S> {
    val startState: S
    val acceptingState: S

    fun transitions(state: S, input: Char): Set<S>
    fun eTransitions(state: S): Set<S>
}