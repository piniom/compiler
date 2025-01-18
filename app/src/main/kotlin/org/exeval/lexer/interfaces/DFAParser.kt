package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.interfaces.NFA

interface DFAParser<S> {
	fun parse(nfa: NFA<S>): DFA<*>
}
