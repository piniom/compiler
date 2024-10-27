package org.exeval.lexer

import org.exeval.parser.utilities.GrammarAnalyser
import org.exeval.parser.utilities.*
import org.exeval.parser.Grammar
import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Production
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GrammarAnalyzerTest {
    
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
            mapOf('A' to listOf('a')),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
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
                'A' to listOf('a', 'b', 'c'),
                'B' to listOf('b', 'c'),
                'C' to listOf('c')
            ),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
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
                'A' to listOf('b', 'c', 'd'),
                'B' to listOf('b'),
                'C' to listOf('c'),
                'D' to listOf('d')
            ),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
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
                'A' to listOf('c', 'd'),
                'B' to listOf('c', 'd'),
                'C' to listOf('c', 'd'),
                'D' to listOf('d')
            ),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
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
                'A' to listOf('b', 'c'),
                'B' to listOf('b'),
                'C' to listOf('c'),
            ),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
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
                'A' to listOf('c', 'd', 'e'),
                'B' to listOf('c', 'd', 'e'),
                'C' to listOf('c', 'd', 'e'),
                'D' to listOf('d')
            ),
            grammar
        )
        assertEquals(analyzedGrammar, GrammarAnalyser.analyseGrammar(grammar))
    }
}
