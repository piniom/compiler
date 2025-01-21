package org.exeval.parser.parser

import org.exeval.input.interfaces.Location
import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Production
import org.exeval.parser.interfaces.ParseTree

class Parser<S>(
    private val oneTimeParserFactory: OneTimeParser.Factory<S>, private val startProduction: Production<S>
) {
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

    interface Factory<S> {
        fun create(analyzedGrammar: AnalyzedGrammar<S>): Parser<S>
    }
}

class ParseError(override val message: String, val startErrorLocation: Location, val endErrorLocation: Location) :
    Exception()