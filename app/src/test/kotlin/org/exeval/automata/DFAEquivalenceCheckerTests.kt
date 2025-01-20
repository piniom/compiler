package org.exeval.automata

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.providers.SampleDFAProvider
import org.exeval.automata.tools.DFAEquivalenceCheckerTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DFAEquivalentCheckerTests {
	private var automaProvider = SampleDFAProvider()

	val allAutomatas =
		listOf(
			automaProvider.emptyIntDFA,
			automaProvider.emptyStringDFA,
			automaProvider.aStarIntDFA,
			automaProvider.aStarStringDFA,
			automaProvider.aLastAcceptingIntDFA,
			automaProvider.bLastAcceptingStringDFA,
			automaProvider.abStarStringDFA,
			automaProvider.baStarIntDFA,
		)

	val equivalentMap =
		mapOf(
			automaProvider.emptyIntDFA to listOf(automaProvider.emptyIntDFA, automaProvider.emptyStringDFA),
			automaProvider.emptyStringDFA to listOf(automaProvider.emptyIntDFA, automaProvider.emptyStringDFA),
			automaProvider.aStarIntDFA to listOf(automaProvider.aStarIntDFA, automaProvider.aStarStringDFA),
			automaProvider.aStarStringDFA to listOf(automaProvider.aStarIntDFA, automaProvider.aStarStringDFA),
			automaProvider.aLastAcceptingIntDFA to listOf(automaProvider.aLastAcceptingIntDFA),
			automaProvider.bLastAcceptingStringDFA to listOf(automaProvider.bLastAcceptingStringDFA),
			automaProvider.abStarStringDFA to listOf(automaProvider.abStarStringDFA),
			automaProvider.baStarIntDFA to listOf(automaProvider.baStarIntDFA),
		)

	private fun <T, S> testAutomatas(
		dfa1: DFA<T>,
		dfa2: DFA<S>,
		expected: Boolean,
	) {
		val dfaChecker = DFAEquivalenceCheckerTool()
		assertEquals(expected, dfaChecker.areEquivalent(dfa1, dfa2))
	}

	@Test
	fun dfaAreEquivalentIfTheyAreTheSame() {
		for (automa in allAutomatas)
			testAutomatas(automa, automa, true)
	}

	@Test
	fun checkAllSampleDfaInMapAreEquivalent() {
		for (entry in equivalentMap.entries.iterator())
			for (automa in entry.value)
				testAutomatas(entry.key, automa, true)
	}

	@Test
	fun checkAllSampleDfaNotInMapAreNotEquivalent() {
		for (entry in equivalentMap.entries.iterator())
			for (automa in allAutomatas)
				if (!entry.value.contains(automa)) {
					testAutomatas(entry.key, automa, false)
				}
	}
}
