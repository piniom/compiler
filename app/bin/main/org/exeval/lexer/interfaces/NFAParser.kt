package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.Regex
import org.exeval.automata.interfaces.NFA

interface NFAParser {
    fun parse(regex: Regex): NFA<*>
}
