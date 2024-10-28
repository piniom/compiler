package org.exeval.parser.utilities

import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Grammar

class GrammarAnalyser {
    /*
    note to testers:
    empty symbol can be set, or can be denoted as empty transition ('A'->list())
    */
    companion object {
        private fun <C> getNullable(grammar: Map<C, Set<List<C>>>, empty: C?): Set<C> {
            var nullable: MutableSet<C> = if (empty != null) mutableSetOf(empty) else mutableSetOf()
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

        fun <C> analyseGrammar(grammar: Grammar<C>, empty: C? = null): AnalyzedGrammar<C> {
            val transitions: MutableMap<C, MutableSet<List<C>>> = mutableMapOf()
            grammar.productions.forEach {
                transitions.getOrPut(it.left) { mutableSetOf() }.add(it.right)
            }

            val nullable = getNullable(transitions, empty)

            return AnalyzedGrammar(
                nullable, createFirstSet(grammar, nullable), grammar
            )
        }
    }
}