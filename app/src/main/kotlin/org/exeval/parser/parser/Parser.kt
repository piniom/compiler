package org.exeval.parser.parser

import org.exeval.input.interfaces.Location
import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Production
import org.exeval.parser.interfaces.ParseTree
import org.exeval.parser.parser.impls.BigTablesCreator
import org.exeval.parser.parser.impls.SimpleTablesCreator
import org.exeval.parser.parser.impls.LR1OneTimeParser

class Parser<S>(analyzedGrammar: AnalyzedGrammar<S>) {
    private val oneTimeParserFactory: OneTimeParser.Factory<S>
    private val startProduction: Production<S>
    private val tablesCreator: TablesCreator<S, Int> = SimpleTablesCreator(BigTablesCreator(analyzedGrammar))

    init {
        val startSymbol = analyzedGrammar.grammar.startSymbol
        val endSymbol = analyzedGrammar.grammar.endOfParse
        startProduction = analyzedGrammar.grammar.productions.find { production -> production.left == startSymbol }
            ?: throw IllegalStateException("No production where left=startSymbol")

        val tables = tablesCreator.tables
        oneTimeParserFactory = LR1OneTimeParser.Factory(startSymbol, endSymbol, tables)
    }

    fun run(leaves: List<ParseTree.Leaf<S>>): ParseTree<S> {
        val startLocation = leaves.first().startLocation
        val endLocation = leaves.last().endLocation

        val oneTimeParser = oneTimeParserFactory.create(leaves)
        val treeWithoutStart = oneTimeParser.run()
        val resTree = ParseTree.Branch(
            startProduction,
            children = listOf(treeWithoutStart),
            startLocation = startLocation,
            endLocation = endLocation
        )

        return resTree
    }

}

class ParseError(override val message: String, val startErrorLocation: Location, val endErrorLocation: Location) :
    Exception()