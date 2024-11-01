package org.exeval.parser

class ParserParenthesisTest {
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
}