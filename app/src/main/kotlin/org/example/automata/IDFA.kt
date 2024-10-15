package org.example.automata

interface IDFA<S> {
    interface IDFA<S> {
        val startState: S

        fun isDead(state: S): Boolean
        fun isAccepting(state: S): Boolean
        fun transitions(state: S, input: Char): S?
    }
}