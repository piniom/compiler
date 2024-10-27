package org.exeval.lexer

import org.exeval.parser.utilities.GrammarAnalyser
import org.exeval.parser.utilities.*
import org.exeval.parser.Grammar
import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Production
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.collections.component2

class GrammarAnalyzerTest {
    
    fun grammarsEqual(g1: AnalyzedGrammar<Char>, g2: AnalyzedGrammar<Char>): Boolean {
        return g1.nullable == g2.nullable && g1.firstProduct.map { it.component2().sorted() } == g2.firstProduct.map { it.component2().sorted() } && g1.grammar == g2.grammar
    }

    @Test
    fun oneProductionNullableWithOneFirstSymbolTest() {
        val grammar = Grammar(
            'A',
            '-',
            listOf(
                Production('A', listOf('a')),
                Production('A', listOf())
            )
        )
        val analyzedGrammar = AnalyzedGrammar(
            setOf('A'),
            mapOf('A' to listOf('A', 'a')),
            grammar
        )
        assert(grammarsEqual(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar)))
    }
    
    @Test
    fun simpleNullableProductionsChainWithFirstSymbolsTest() {
        val grammar = Grammar(
            'A',
            '-',
            listOf(
                Production('A', listOf('a')),
                Production('A', listOf('B')),
                Production('B', listOf('b')),
                Production('B', listOf('C')),
                Production('C', listOf('c')),
                Production('C', listOf()),
            )
        )
        val analyzedGrammar = AnalyzedGrammar(
            setOf('A', 'B', 'C'),
            mapOf(
                'A' to listOf('A', 'a', 'B', 'b', 'C', 'c'),
                'B' to listOf('B', 'b', 'C', 'c'),
                'C' to listOf('C', 'c')
            ),
            grammar
        )
        assert(grammarsEqual(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar)))
    }
    
    @Test
    fun firstSymbolWhenMultipleSymbolsOnTheLeftNullableTest() {
        val grammar = Grammar(
            'A',
            '-',
            listOf(
                Production('A', listOf('B', 'C', 'D')),
                Production('B', listOf('b')),
                Production('B', listOf()),
                Production('C', listOf('c')),
                Production('C', listOf()),
                Production('D', listOf('d'))
            )
        )
        val analyzedGrammar = AnalyzedGrammar(
            setOf('B', 'C'),
            mapOf(
                'A' to listOf('A', 'B', 'b', 'C', 'c', 'D', 'd'),
                'B' to listOf('B', 'b'),
                'C' to listOf('C', 'c'),
                'D' to listOf('D', 'd')
            ),
            grammar
        )
        assert(grammarsEqual(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar)))
    }
    
    @Test
    fun firstSymbolsFromDifferentProductionAlternativesTest() {
        val grammar = Grammar(
            'A',
            '-',
            listOf(
                Production('A', listOf('B', 'C', 'D')),
                Production('B', listOf('C')),
                Production('B', listOf('D')),
                Production('C', listOf('c')),
                Production('C', listOf('D')),
                Production('D', listOf('d')),
                Production('D', listOf())
            )
        )
        val analyzedGrammar = AnalyzedGrammar(
            setOf('A', 'B', 'C', 'D'),
            mapOf(
                'A' to listOf('A', 'B', 'C', 'c', 'D', 'd'),
                'B' to listOf('B', 'C', 'c', 'D', 'd'),
                'C' to listOf('C', 'c', 'D', 'd'),
                'D' to listOf('D', 'd')
            ),
            grammar
        )
        assert(grammarsEqual(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar)))
    }
    
    @Test
    fun recursiveProductionOtherAlternativeNotNullableTest() {
        val grammar = Grammar(
            'A',
            '-',
            listOf(
                Production('A', listOf('B', 'A')),
                Production('A', listOf('C')),
                Production('B', listOf('b')),
                Production('B', listOf()),
                Production('C', listOf('c'))
            )
        )
        val analyzedGrammar = AnalyzedGrammar(
            setOf('B'),
            mapOf(
                'A' to listOf('A', 'B', 'b', 'C', 'c'),
                'B' to listOf('B', 'b'),
                'C' to listOf('C', 'c'),
            ),
            grammar
        )
        assert(grammarsEqual(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar)))
    }
    
    @Test
    fun complexNullableAndFirstSymbolsWithMutualRecursionTest() {
        val grammar = Grammar(
            'A',
            '-',
            listOf(
                Production('A', listOf('B', 'A', 'D')),
                Production('A', listOf()),
                Production('B', listOf('A', 'C', 'A')),
                Production('C', listOf('c')),
                Production('C', listOf('D', 'e')),
                Production('D', listOf('d')),
                Production('D', listOf())
            )
        )
        val analyzedGrammar = AnalyzedGrammar(
            setOf('A', 'D'),
            mapOf(
                'A' to listOf('A', 'B', 'c', 'D', 'd', 'e'),
                'B' to listOf('A', 'B', 'C', 'c', 'd', 'e'),
                'C' to listOf('C', 'c', 'D', 'd', 'e'),
                'D' to listOf('D', 'd')
            ),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
    }
}
