package org.example.lexer

import org.example.automata.IDFA
import org.example.automata.INFA

interface IDFAParser<S> {
    fun parse(nfa: INFA<S>): IDFA<S>
}