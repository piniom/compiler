package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.junit.jupiter.api.Test

class DFAminTest {
	class minDFA<S> : DFA<S> {
		override val startState: S
		var acceptStates = mutableSetOf<S>()
		public var tmap = mutableMapOf<S, MutableMap<Char, S>>()
		constructor(start: S, acs: MutableSet<S>, t: MutableMap<S, MutableMap<Char, S>>) {
			startState = start
			acceptStates = acs
			tmap = t
		}

		override fun isDead(state: S): Boolean {
			// a quick note: dead states will be consolidated during minimization
			return !isAccepting(state) && transitions(state).all { it.value == state }
		}

		override fun isAccepting(state: S): Boolean {
			return acceptStates.contains(state)
		}

		override fun transitions(state: S): Map<Char, S> {
			return tmap[state]!!.toMap()
		}
	}

	@Test
	fun noMergeTest() {
		var start = "A"
		var acs = mutableSetOf("D")
		var t =
			mutableMapOf(
				"A" to mutableMapOf('0' to "B", '1' to "C"),
				"B" to mutableMapOf('0' to "D", '1' to "D"),
				"C" to mutableMapOf('0' to "D", '1' to "D"),
				"D" to mutableMapOf('0' to "A", '1' to "A"),
			)
		var minobj = DFAmin<String>()
		var base = minDFA<String>(start, acs, t)
		var min = minobj.minimize(base)
		assert(minobj.getStates(min).size == 3)
	}

	@Test
	fun unreachableStatesTest() {
		var start = "A"
		var accepting = mutableSetOf("B")
		var transitions =
			mutableMapOf(
				"A" to mutableMapOf('0' to "B", '1' to "C"),
				"B" to mutableMapOf('0' to "A", '1' to "C"),
				"C" to mutableMapOf('0' to "B", '1' to "A"),
				"E" to mutableMapOf('0' to "F"),
				"F" to mutableMapOf('0' to "E"),
			)

		var minobj = DFAmin<String>()
		var base = minDFA<String>(start, accepting, transitions)
		var min = minobj.minimize(base)

		assert(minobj.getStates(min).size == 2)
	}

	@Test
	fun cascadeMergeTest() {
		var start = "A"
		var accepting = mutableSetOf("X")
		var transitions =
			mutableMapOf(
				"A" to mutableMapOf('0' to "B", '1' to "C"),
				"B" to mutableMapOf('0' to "D", '1' to "A"),
				"C" to mutableMapOf('0' to "F", '1' to "A"),
				"D" to mutableMapOf('0' to "X"),
				"F" to mutableMapOf('0' to "X"),
				"X" to mutableMapOf('0' to "A"),
			)

		var minobj = DFAmin<String>()
		var base = minDFA<String>(start, accepting, transitions)
		var min = minobj.minimize(base)

		assert(minobj.getStates(min).size == 4)
	}

	@Test
	fun stateAfterAcceptingTest() {
		var start = "A"
		var accepting = mutableSetOf("C")
		var transitions =
			mutableMapOf(
				"A" to mutableMapOf('0' to "B"),
				"B" to mutableMapOf('0' to "C"),
				"C" to mutableMapOf('0' to "D"),
				"D" to mutableMapOf('0' to "E"),
				"E" to mutableMapOf('0' to "F"),
				"F" to mutableMapOf('0' to "D"),
			)

		var minobj = DFAmin<String>()
		var base = minDFA<String>(start, accepting, transitions)
		var min = minobj.minimize(base)

		assert(minobj.getStates(min).size == 4)
	}

	@Test
	fun deadStateTest() {
		var start = "A"
		var accepting = mutableSetOf("C")
		var transitions =
			mutableMapOf(
				"A" to mutableMapOf('0' to "B"),
				"B" to mutableMapOf('0' to "C"),
				"C" to mutableMapOf('0' to "A"),
				"D" to mutableMapOf('0' to "F"),
				"F" to mutableMapOf('0' to "D"),
			)

		var minobj = DFAmin<String>()
		var base = minDFA<String>(start, accepting, transitions)
		var min = minobj.minimize(base)

		assert(minobj.getStates(min).size == 3)
	}

	@Test
	fun notMinimizationTest() {
		var start = "A"
		var accepting = mutableSetOf("D")
		var transitions =
			mutableMapOf(
				"A" to mutableMapOf('0' to "B", '1' to "C", '2' to "D"),
				"B" to mutableMapOf('0' to "C", '1' to "D"),
				"C" to mutableMapOf('0' to "D"),
				"D" to mutableMapOf('0' to "A"),
			)

		var minobj = DFAmin<String>()
		var base = minDFA<String>(start, accepting, transitions)
		var min = minobj.minimize(base)

		assert(minobj.getStates(min).size == 4)
	}
}
