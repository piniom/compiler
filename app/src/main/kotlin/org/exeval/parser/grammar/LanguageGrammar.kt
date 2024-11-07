package org.exeval.parser.grammar

import org.exeval.parser.Production
import org.exeval.parser.Grammar
import org.exeval.parser.Parser

interface GrammarSymbol {
	fun productions(): List<List<GrammarSymbol>>
}

interface Terminal : GrammarSymbol {
	override fun productions(): List<List<GrammarSymbol>> = listOf()
}

interface TerminalGroup : GrammarSymbol {
	fun values(): List<Terminal>
	override fun productions(): List<List<GrammarSymbol>> = values().map{ listOf(it) }
}

fun getAllProductions(startSymbol: GrammarSymbol): List<Production<GrammarSymbol>> {
	fun getSymbolProductions(symbol: GrammarSymbol, processedSymbols: MutableSet<GrammarSymbol>, productions: MutableList<Production<GrammarSymbol>>) {
		if (!processedSymbols.contains(symbol)) {
			processedSymbols.add(symbol)
			productions += symbol.productions().map { Production(symbol, it) }
			for (production in symbol.productions()) {
				for (nextSymbol in production) {
					getSymbolProductions(nextSymbol, processedSymbols, productions)
				}
			}
		}
	}

	var processedSymbols: MutableSet<GrammarSymbol> = mutableSetOf()
	var productions: MutableList<Production<GrammarSymbol>> = mutableListOf()

	getSymbolProductions(startSymbol, processedSymbols, productions)

	return productions
}

/*
val LanguageGrammar = Grammar<GrammarSymbol>(
	Program,
	ErrorSymbol,
	getAllProductions(Program)
)
*/

val LanguageGrammar = Grammar<GrammarSymbol>(
	Expression,
	ErrorSymbol,
	getAllProductions(Expression)
)

val analyzed = org.exeval.parser.utilities.GrammarAnalyser.analyseGrammar(LanguageGrammar)

fun print() {
	println(analyzed)
	val parser = Parser(analyzed)
}
