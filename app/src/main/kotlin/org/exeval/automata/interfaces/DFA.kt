package org.exeval.automata.interfaces

interface DFA<S> {
	val startState: S

	fun isDead(state: S): Boolean

	fun isAccepting(state: S): Boolean

	fun transitions(state: S): Map<Char, S>
}
