package org.exeval.parser.parser.impls

import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.parser.Parser

class BigParserFactory<S> : Parser.Factory<S> {
    override fun create(analyzedGrammar: AnalyzedGrammar<S>): Parser<S> {
        val startSymbol = analyzedGrammar.grammar.startSymbol
        val endSymbol = analyzedGrammar.grammar.endOfParse
        val startProduction = analyzedGrammar.grammar.productions.find { production -> production.left == startSymbol }
            ?: throw IllegalStateException("No production where left=startSymbol")

        val tables = BigTablesCreator(analyzedGrammar).tables
        val oneTimeParserFactory = LR1OneTimeParser.Factory(startSymbol, endSymbol, tables)

        return Parser(oneTimeParserFactory, startProduction)
    }
}