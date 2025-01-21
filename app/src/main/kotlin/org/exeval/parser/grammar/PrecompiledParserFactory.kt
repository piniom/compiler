package org.exeval.parser.grammar

import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.parser.Parser
import org.exeval.parser.parser.impls.BigTablesCreator
import org.exeval.parser.parser.impls.FileTablesCreator
import org.exeval.parser.parser.impls.LR1OneTimeParser
import org.exeval.parser.parser.impls.SimpleTablesCreator

private const val resourcePath = "/parser/serialized-tables.bin"

class PrecompiledParserFactory : Parser.Factory<GrammarSymbol> {
    override fun create(analyzedGrammar: AnalyzedGrammar<GrammarSymbol>): Parser<GrammarSymbol> {
        val startSymbol = analyzedGrammar.grammar.startSymbol
        val endSymbol = analyzedGrammar.grammar.endOfParse
        val startProduction = analyzedGrammar.grammar.productions.find { production -> production.left == startSymbol }
            ?: throw IllegalStateException("No production where left=startSymbol")

        val tables = FileTablesCreator(resourcePath).tables
        val oneTimeParserFactory = LR1OneTimeParser.Factory(startSymbol, endSymbol, tables)

        return Parser(oneTimeParserFactory, startProduction)
    }

    fun createNewTables(analyzedGrammar: AnalyzedGrammar<GrammarSymbol>) {
        val tables = SimpleTablesCreator(BigTablesCreator(analyzedGrammar)).tables
        FileTablesCreator.saveTablesToFile(tables, resourcePath)
    }
}