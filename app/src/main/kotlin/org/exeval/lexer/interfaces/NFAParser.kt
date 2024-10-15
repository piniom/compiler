package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.Regex
import org.exeval.automata.interfaces.NFA

interface NFAParser<S> {
    fun parse(regex: Regex): NFA<S>
}