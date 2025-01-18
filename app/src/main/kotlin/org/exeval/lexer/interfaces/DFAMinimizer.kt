package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.DFA

interface DFAMinimizer<S> {
	fun minimize(dfa: DFA<S>): DFA<*>
}
