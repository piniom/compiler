package org.example.automata

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

// TODO: Delete it after stage2 introduction commit
interface IDFA<S> {
    val startState: S
    fun isDead(state: S): Boolean
    fun isAccepting(state: S): Boolean
    fun transitions(state: S): Map<Char, S>
}

class DFAEquivalentCheckerTests {

    private var automaProvider = SampleDFAProvider()

    val allAutomatas = listOf(automaProvider.emptyIntDFA, automaProvider.emptyStringDFA,
    automaProvider.aStarIntDFA, automaProvider.aStarStringDFA, automaProvider.aLastAcceptingIntDFA,
    automaProvider.bLastAcceptingStringDFA, automaProvider.abStarStringDFA,
    automaProvider.baStarIntDFA)

    val equivalentMap = mapOf(automaProvider.emptyIntDFA to listOf(automaProvider.emptyIntDFA, automaProvider.emptyStringDFA),
     automaProvider.emptyStringDFA to listOf(automaProvider.emptyIntDFA, automaProvider.emptyStringDFA),
     automaProvider.aStarIntDFA to listOf(automaProvider.aStarIntDFA, automaProvider.aStarStringDFA),
     automaProvider.aStarStringDFA to listOf(automaProvider.aStarIntDFA, automaProvider.aStarStringDFA),
     automaProvider.aLastAcceptingIntDFA to listOf(automaProvider.aLastAcceptingIntDFA),
     automaProvider.bLastAcceptingStringDFA to listOf(automaProvider.bLastAcceptingStringDFA),
     automaProvider.abStarStringDFA to listOf(automaProvider.abStarStringDFA),
     automaProvider.baStarIntDFA to listOf(automaProvider.baStarIntDFA))

    private fun <T, S>testAutomatas(dfa1: IDFA<T>, dfa2: IDFA<S>, expected: Boolean) {
        val dfaChecker = DFAEquivalentChecker()
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
                if (!entry.value.contains(automa))
                    testAutomatas(entry.key, automa, false)
    }

}