package org.exeval.parser

import org.exeval.input.interfaces.Location
import org.exeval.parser.Parser.Action
import org.exeval.parser.Parser.Tables
import org.exeval.parser.interfaces.ParseTree
import org.exeval.parser.utilities.RawParser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class RawParserParenthesisTest {
    // ParenthesisSymbol - ParSym, it's symbol in the grammar that codes valid parenthesis sequences
    private enum class ParSym {
        LIST, PAIR, OPEN, CLOSE, END
    }

    private val grammar = Grammar(
        startSymbol = ParSym.LIST, endOfParse = ParSym.END, productions = listOf(
            Production(ParSym.LIST, listOf(ParSym.LIST, ParSym.PAIR)),
            Production(ParSym.LIST, listOf(ParSym.PAIR)),
            Production(ParSym.PAIR, listOf(ParSym.OPEN, ParSym.LIST, ParSym.CLOSE)),
            Production(ParSym.PAIR, listOf(ParSym.OPEN, ParSym.CLOSE)),
        )
    )

    private val actions: Map<Pair<ParSym, Int>, Action<ParSym, Int>> = mapOf(
        (ParSym.END to 1) to Action.Accept(),
        (ParSym.END to 2) to Action.Reduce(grammar.productions[1]),
        (ParSym.END to 4) to Action.Reduce(grammar.productions[0]),
        (ParSym.END to 8) to Action.Reduce(grammar.productions[3]),
        (ParSym.END to 10) to Action.Reduce(grammar.productions[2]),

        (ParSym.OPEN to 0) to Action.Shift(3),
        (ParSym.OPEN to 1) to Action.Shift(3),
        (ParSym.OPEN to 2) to Action.Reduce(grammar.productions[1]),
        (ParSym.OPEN to 3) to Action.Shift(7),
        (ParSym.OPEN to 4) to Action.Reduce(grammar.productions[0]),
        (ParSym.OPEN to 5) to Action.Shift(7),
        (ParSym.OPEN to 6) to Action.Reduce(grammar.productions[1]),
        (ParSym.OPEN to 7) to Action.Shift(7),
        (ParSym.OPEN to 8) to Action.Reduce(grammar.productions[3]),
        (ParSym.OPEN to 9) to Action.Reduce(grammar.productions[0]),
        (ParSym.OPEN to 10) to Action.Reduce(grammar.productions[2]),
        (ParSym.OPEN to 11) to Action.Shift(7),
        (ParSym.OPEN to 12) to Action.Reduce(grammar.productions[3]),
        (ParSym.OPEN to 13) to Action.Reduce(grammar.productions[2]),

        (ParSym.CLOSE to 3) to Action.Shift(8),
        (ParSym.CLOSE to 5) to Action.Shift(10),
        (ParSym.CLOSE to 6) to Action.Reduce(grammar.productions[1]),
        (ParSym.CLOSE to 7) to Action.Shift(12),
        (ParSym.CLOSE to 9) to Action.Reduce(grammar.productions[0]),
        (ParSym.CLOSE to 11) to Action.Shift(13),
        (ParSym.CLOSE to 12) to Action.Reduce(grammar.productions[3]),
        (ParSym.CLOSE to 13) to Action.Reduce(grammar.productions[2]),
    )
    private val goto: Map<Pair<ParSym, Int>, Int> = mapOf(
        (ParSym.LIST to 0) to 1,
        (ParSym.LIST to 3) to 5,
        (ParSym.LIST to 7) to 11,
        (ParSym.PAIR to 0) to 2,
        (ParSym.PAIR to 1) to 4,
        (ParSym.PAIR to 3) to 6,
        (ParSym.PAIR to 5) to 9,
        (ParSym.PAIR to 7) to 6,
        (ParSym.PAIR to 11) to 9,
    )
    private val parser = RawParser<ParSym, Int>(grammar.startSymbol, grammar.endOfParse, Tables(0, actions, goto))

    @Test
    fun `() works`() {
        val leaves = stringToLeaves("()")

        val expected = ParseTree.Branch(
            grammar.productions[1], listOf(
                ParseTree.Branch(
                    grammar.productions[3], listOf(
                        ParseTree.Leaf(ParSym.OPEN, 0.toLoc(), 0.toLoc()),
                        ParseTree.Leaf(ParSym.CLOSE, 1.toLoc(), 1.toLoc())
                    ), 0.toLoc(), 1.toLoc()
                )
            ), 0.toLoc(), 1.toLoc()
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
                                ParseTree.Leaf(ParSym.OPEN, 0.toLoc(), 0.toLoc()),
                                ParseTree.Branch(
                                    grammar.productions[1], listOf(
                                        ParseTree.Branch(
                                            grammar.productions[3], listOf(
                                                ParseTree.Leaf(ParSym.OPEN, 1.toLoc(), 1.toLoc()),
                                                ParseTree.Leaf(ParSym.CLOSE, 2.toLoc(), 2.toLoc())
                                            ), 1.toLoc(), 2.toLoc()
                                        )
                                    ), 1.toLoc(), 2.toLoc()
                                ),
                                ParseTree.Leaf(ParSym.CLOSE, 3.toLoc(), 3.toLoc()),
                            ), 0.toLoc(), 3.toLoc()
                        )
                    ), 0.toLoc(), 3.toLoc()
                ), ParseTree.Branch(
                    grammar.productions[3], listOf(
                        ParseTree.Leaf(ParSym.OPEN, 4.toLoc(), 4.toLoc()),
                        ParseTree.Leaf(ParSym.CLOSE, 5.toLoc(), 5.toLoc())
                    ), 4.toLoc(), 5.toLoc()
                )
            ), 0.toLoc(), 5.toLoc()
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
            val loc = Loc(i)
            val res: ParseTree.Leaf<ParSym> = when (char) {
                '(' -> ParseTree.Leaf(ParSym.OPEN, loc, loc)
                ')' -> ParseTree.Leaf(ParSym.CLOSE, loc, loc)
                else -> throw Exception("Filter does not work...")
            }
            res
        }
        val endLoc = Loc(body.size)
        val tail = listOf(ParseTree.Leaf(ParSym.END, endLoc, endLoc))

        return body + tail
    }
}

private data class Loc(
    override val idx: Int
) : Location {
    override val line = 0
}

private fun Int.toLoc(): Loc {
    return Loc(this)
}