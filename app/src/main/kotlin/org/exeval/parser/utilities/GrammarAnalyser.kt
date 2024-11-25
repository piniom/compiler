package org.exeval.parser.utilities

import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Grammar

class GrammarAnalyser {
    /*
    note to testers:
    empty symbol can be set, or can be denoted as empty transition ('A'->list())
    */
    private fun <C> getNullable(grammar: Map<C, Set<List<C>>>): Set<C> {
        var nullable: MutableSet<C> = mutableSetOf()
        while (true) {
            val newNullable = nullable.toMutableSet()
            grammar.forEach {
                val c = it.key
                if (it.value.any {
                        it.all {
                            nullable.contains(it)
                        }
                    }) {
                    newNullable.add(c)
                }
            }
            if (newNullable == nullable) {
                break
            } else {
                nullable = newNullable
            }
        }
        return nullable
    }

    fun <C> analyseGrammar(grammar: Grammar<C>): AnalyzedGrammar<C> {
        val transitions: MutableMap<C, MutableSet<List<C>>> = mutableMapOf()
        grammar.productions.forEach {
            transitions.getOrPut(it.left) { mutableSetOf() }.add(it.right)
        }

        val nullable = NullableGrammarCreator().getNullable(transitions)

        return AnalyzedGrammar(
            nullable, 
            FirstSetGrammarCreator().createFirstSet(grammar, nullable),
            grammar
        )
    }
}