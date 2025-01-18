package org.exeval.parser

import org.exeval.input.interfaces.Location
import org.exeval.parser.interfaces.ParseTree
import org.exeval.parser.parser.ParseError
import org.exeval.parser.parser.impls.BigParserFactory
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class FullParserTest {
    private enum class ParSym {
        START, LIST, PAIR, OPEN, CLOSE, END
    }

    private val grammar = Grammar(
        startSymbol = ParSym.START, endOfParse = ParSym.END, productions = listOf(
            Production(ParSym.START, listOf(ParSym.LIST)),
            Production(ParSym.LIST, listOf(ParSym.LIST, ParSym.PAIR)),
            Production(ParSym.LIST, listOf(ParSym.PAIR)),
            Production(ParSym.PAIR, listOf(ParSym.OPEN, ParSym.LIST, ParSym.CLOSE)),
            Production(ParSym.PAIR, listOf(ParSym.OPEN, ParSym.CLOSE)),
        )
    )
    private val analyzedGrammar = AnalyzedGrammar(
        nullable = setOf(), firstProduct = mapOf(
            ParSym.START to setOf(ParSym.START, ParSym.PAIR, ParSym.LIST, ParSym.OPEN),
            ParSym.LIST to setOf(ParSym.LIST, ParSym.PAIR, ParSym.OPEN),
            ParSym.PAIR to setOf(ParSym.PAIR, ParSym.OPEN)
        ), grammar = grammar
    )

    private val parser = BigParserFactory<ParSym>().create(analyzedGrammar)

    @Test
    fun `() works`() {
        val leaves = stringToLeaves("()")

        val expected = ParseTree.Branch(
            grammar.productions[0], listOf(
                ParseTree.Branch(
                    grammar.productions[2], listOf(
                        ParseTree.Branch(
                            grammar.productions[4], listOf(
                                ParseTree.Leaf(ParSym.OPEN, 0.toLoc(), 1.toLoc()),
                                ParseTree.Leaf(ParSym.CLOSE, 1.toLoc(), 2.toLoc())
                            ), 0.toLoc(), 2.toLoc()
                        )
                    ), 0.toLoc(), 2.toLoc()
                )
            ), 0.toLoc(), 2.toLoc()
        )

        val actual = parser.run(leaves)

        assertEquals(expected, actual)
    }

    @Test
    fun `(())() works`() {
        val leaves = stringToLeaves("(())()")
        val expected = ParseTree.Branch(
            grammar.productions[0], listOf(
                ParseTree.Branch(
                    grammar.productions[1], listOf(
                        ParseTree.Branch(
                            grammar.productions[2], listOf(
                                ParseTree.Branch(
                                    grammar.productions[3], listOf(
                                        ParseTree.Leaf(ParSym.OPEN, 0.toLoc(), 1.toLoc()),
                                        ParseTree.Branch(
                                            grammar.productions[2], listOf(
                                                ParseTree.Branch(
                                                    grammar.productions[4], listOf(
                                                        ParseTree.Leaf(ParSym.OPEN, 1.toLoc(), 2.toLoc()),
                                                        ParseTree.Leaf(ParSym.CLOSE, 2.toLoc(), 3.toLoc())
                                                    ), 1.toLoc(), 3.toLoc()
                                                )
                                            ), 1.toLoc(), 3.toLoc()
                                        ),
                                        ParseTree.Leaf(ParSym.CLOSE, 3.toLoc(), 4.toLoc()),
                                    ), 0.toLoc(), 4.toLoc()
                                )
                            ), 0.toLoc(), 4.toLoc()
                        ), ParseTree.Branch(
                            grammar.productions[4], listOf(
                                ParseTree.Leaf(ParSym.OPEN, 4.toLoc(), 5.toLoc()),
                                ParseTree.Leaf(ParSym.CLOSE, 5.toLoc(), 6.toLoc())
                            ), 4.toLoc(), 6.toLoc()
                        )
                    ), 0.toLoc(), 6.toLoc()
                )
            ), 0.toLoc(), 6.toLoc()
        )

        val actual = parser.run(leaves)

        assertEquals(expected, actual)
    }

    @Test
    fun `()) throws error`() {
        val leaves = stringToLeaves("())")

        assertThrows<ParseError> { parser.run(leaves) }
    }

    @Test
    fun `(() throws error`() {
        val leaves = stringToLeaves("(()")

        assertThrows<ParseError> { parser.run(leaves) }
    }

    private fun stringToLeaves(str: String): List<ParseTree.Leaf<ParSym>> {
        val body = str.filter { c -> c == '(' || c == ')' }.mapIndexed { i, char ->
            val loc = FLoc(i)
            val nextLoc = FLoc(i+1)
            val res: ParseTree.Leaf<ParSym> = when (char) {
                '(' -> ParseTree.Leaf(ParSym.OPEN, loc, nextLoc)
                ')' -> ParseTree.Leaf(ParSym.CLOSE, loc, nextLoc)
                else -> throw Exception("Filter does not work...")
            }
            res
        }
        val endLoc = FLoc(body.size)
        val tail = listOf(ParseTree.Leaf(ParSym.END, endLoc, endLoc))

        return body + tail
    }
}

private data class FLoc(
    override var idx: Int,
) : Location {
    override var line = 0
}

private fun Int.toLoc(): FLoc {
    return FLoc(this)
}