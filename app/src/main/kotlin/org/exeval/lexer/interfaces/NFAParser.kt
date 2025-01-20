package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.NFA
import org.exeval.automata.interfaces.Regex

interface NFAParser {
	fun parse(regex: Regex): NFA<*>
}
