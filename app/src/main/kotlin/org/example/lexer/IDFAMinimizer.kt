package org.example.lexer

import org.example.automata.IDFA

interface IDFAMinimizer<S> {
    fun minimize(dfa: IDFA<S>): IDFA<S>
}