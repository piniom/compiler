package org.exeval.parser

import org.exeval.input.interfaces.Location
import org.exeval.parser.interfaces.ParseTree
import org.exeval.parser.utilities.RawParser
import org.exeval.parser.utilities.TableCreator

class Parser<S>(
	analyzedGrammar: AnalyzedGrammar<S>,
) {
	private val rawParser: RawParser<S, State<S>>
	private val startProduction: Production<S>
	private val tableCreator: TableCreator<S> = TableCreator(analyzedGrammar)

	init {
		val startSymbol = analyzedGrammar.grammar.startSymbol
		val endSymbol = analyzedGrammar.grammar.endOfParse
		val tables = tableCreator.tables

		rawParser = RawParser(startSymbol, endSymbol, tables)
		startProduction = analyzedGrammar.grammar.productions.find { production ->
			production.left == startSymbol
		} ?: throw IllegalStateException("No production where left=startSymbol")
	}

	fun run(leaves: List<ParseTree.Leaf<S>>): ParseTree<S> {
		val startLocation = leaves.first().startLocation
		val endLocation = leaves.last().endLocation

		val treeWithoutStart = rawParser.run(leaves)
		val resTree =
			ParseTree.Branch<S>(
				startProduction,
				children = listOf(treeWithoutStart),
				startLocation = startLocation,
				endLocation = endLocation,
			)

		return resTree
	}

	data class State<S>(
		val items: Set<LR1Item<S>>,
	)

	data class LR1Item<S>(
		val production: Production<S>,
		val placeholder: Int,
		val lookahead: S,
	)

	sealed interface Action<Symbol, State> {
		data class Reduce<Symbol, State>(
			val production: Production<Symbol>,
		) : Action<Symbol, State>

		data class Shift<Symbol, State>(
			val state: State,
		) : Action<Symbol, State>

		class Accept<Symbol, State> : Action<Symbol, State>
	}

	data class Tables<Symbol, State>(
		val startState: State,
		val actions: Map<Pair<Symbol, State>, Action<Symbol, State>>,
		val goto: Map<Pair<Symbol, State>, State>,
	)
}

class ParseError(
	override val message: String,
	val startErrorLocation: Location,
	val endErrorLocation: Location,
) : Exception()
