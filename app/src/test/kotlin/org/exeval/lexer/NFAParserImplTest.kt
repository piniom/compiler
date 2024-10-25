import org.exeval.lexer.NFAParserImpl
import org.exeval.lexer.interfaces.NFAParser
import org.exeval.automata.interfaces.Regex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NFAParserImplTest {

    private val mockStateFabric: (Int) -> Any = { it }
    private val parser: NFAParser = NFAParserImpl(mockStateFabric)

    @Test
    fun testAtomRegexParsing() {
        val atomRegex = Regex.Atom('a')

        val nfa = parser.parse(atomRegex) as NFAParserImpl.NFAImpl<Any>

        assertEquals(0, nfa.startState)
        assertEquals(1, nfa.acceptingState)
        assertEquals(mapOf('a' to 1), nfa.transitions(0))
        assertEquals(emptySet<Any>(), nfa.eTransitions(0))
    }

    @Test
    fun testStarRegexParsing() {
        val atomRegex = Regex.Atom('a')
        val starRegex = Regex.Star(atomRegex)

        val nfa = parser.parse(starRegex) as NFAParserImpl.NFAImpl<Any>

        assertTrue(nfa.eTransitions(nfa.startState).contains(nfa.acceptingState))
        assertEquals(nfa.eTransitions(nfa.startState).size, 2)
    }

    @Test
    fun testUnionRegexParsing() {
        val atom1 = Regex.Atom('a')
        val atom2 = Regex.Atom('b')
        val unionRegex = Regex.Union(listOf(atom1, atom2))

        val nfa = parser.parse(unionRegex) as NFAParserImpl.NFAImpl<Any>

        assertEquals(0, nfa.startState)
        assertEquals(1, nfa.acceptingState)
        assertEquals(nfa.eTransitions(0).size, 2)
    }

    @Test
    fun testConcatRegexParsing() {
        val atom1 = Regex.Atom('a')
        val atom2 = Regex.Atom('b')
        val concatRegex = Regex.Concat(listOf(atom1, atom2))

        val nfa = parser.parse(concatRegex) as NFAParserImpl.NFAImpl<Any>

        assertEquals(0, nfa.startState)
        assertEquals(1, nfa.acceptingState)
        assertTrue(nfa.eTransitions(0).contains(2))
        assertEquals(nfa.eTransitions(3).size, 1)
    }

    @Test
    fun testComplexRegexUnionAndConcatCombination() {
        val atom1 = Regex.Atom('a')
        val atom2 = Regex.Atom('b')
        val atom3 = Regex.Atom('c')
        val unionConcatRegex = Regex.Union(
            listOf(
                Regex.Concat(listOf(atom1, atom2)),
                atom3
            )
        )

        val nfa = parser.parse(unionConcatRegex) as NFAParserImpl.NFAImpl<Any>

        assertEquals(0, nfa.startState)
        assertEquals(1, nfa.acceptingState)
        assertEquals(nfa.eTransitions(0).size, 2)
    }
}
