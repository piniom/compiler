package org.exeval.parser.grammar

import org.exeval.parser.Parser
import org.exeval.parser.Production
import org.exeval.parser.grammar.GrammarSymbol
import org.exeval.parser.grammar.LanguageGrammar
import org.exeval.parser.grammar.Terminal
import org.exeval.parser.grammar.TerminalGroup
import org.exeval.parser.utilities.GrammarAnalyser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LanguageGrammarTest {
	enum class TestTerminals: Terminal {
		A, B, C
	}

	object TestGroup: TerminalGroup {
		override fun values() = TestTerminals.values().toList()
	}

	object SymbolA: GrammarSymbol {
		override fun productions() = listOf(
			listOf(TestTerminals.A)
		)
	}

	object SymbolB: GrammarSymbol {
		override fun productions() = listOf(
			listOf(TestTerminals.B),
			listOf(
				TestTerminals.C,
				TestGroup,
			),
		)
	}

	object SymbolC: GrammarSymbol {
		override fun productions() = listOf(
			listOf(
				SymbolB,
				SymbolA,
			),
			listOf(InnerSymbol),
		)

		object InnerSymbol: GrammarSymbol {
			override fun productions() = listOf(
				listOf(
					TestTerminals.A,
					TestTerminals.C,
				),
				listOf(
					TestTerminals.A,
					InnerSymbol,
				),
			)
		}
	}

	@Test
	fun productionsOfTerminalGroup() {
		val expectedProductions = listOf(
			listOf(TestTerminals.A),
			listOf(TestTerminals.B),
			listOf(TestTerminals.C),
		)

		assertEquals(expectedProductions, TestGroup.productions())
	}

	@Test
	fun getProductionsTerminalGroup() {
		val expectedProductions = listOf(
			Production(TestGroup, listOf(TestTerminals.A)),
			Production(TestGroup, listOf(TestTerminals.B)),
			Production(TestGroup, listOf(TestTerminals.C)),
		)

		assertEquals(expectedProductions, LanguageGrammar.getAllProductions(TestGroup))
	}

	@Test
	fun getProductionsSimpleSymbol() {
		val expectedProductions = listOf(
			Production(SymbolB, listOf(TestTerminals.B)),
			Production(SymbolB, listOf(TestTerminals.C, TestGroup)),
			Production(TestGroup, listOf(TestTerminals.A)),
			Production(TestGroup, listOf(TestTerminals.B)),
			Production(TestGroup, listOf(TestTerminals.C)),
		)

		assertEquals(expectedProductions, LanguageGrammar.getAllProductions(SymbolB))
	}

	@Test
	fun getProductionsMoreComplexSymbol() {
		val expectedProductions = listOf(
			Production(SymbolC, listOf(SymbolB, SymbolA)),
			Production(SymbolC, listOf(SymbolC.InnerSymbol)),
			Production(SymbolB, listOf(TestTerminals.B)),
			Production(SymbolB, listOf(TestTerminals.C, TestGroup)),
			Production(TestGroup, listOf(TestTerminals.A)),
			Production(TestGroup, listOf(TestTerminals.B)),
			Production(TestGroup, listOf(TestTerminals.C)),
			Production(SymbolA, listOf(TestTerminals.A)),
			Production(SymbolC.InnerSymbol, listOf(TestTerminals.A, TestTerminals.C)),
			Production(SymbolC.InnerSymbol, listOf(TestTerminals.A, SymbolC.InnerSymbol)),
		)

		assertEquals(expectedProductions, LanguageGrammar.getAllProductions(SymbolC))
	}

	@Test
	fun languageGrammarIsUnambiguousLR1() {
		val grammar = LanguageGrammar.grammar
		val analyzedGrammar = GrammarAnalyser.analyseGrammar(grammar)

		assertDoesNotThrow({ Parser(analyzedGrammar) })
	}
}
