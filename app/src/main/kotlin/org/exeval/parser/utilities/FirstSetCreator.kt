package org.exeval.parser.utilities

import org.exeval.parser.Grammar

fun <S> createFirstSet(grammar: Grammar<S>, nullable: Set<S>): Map<S, Set<S>> {
    return transitiveClosure(
        addPossibleStates(
            crateMapWithOnlyKeySymbol(grammar), grammar, nullable
        )
    ).mapValues { it.value.toSet() }
}

private fun <S> crateMapWithOnlyKeySymbol(grammar: Grammar<S>): Map<S, MutableSet<S>> {
    return grammar.productions.map { it.left }.associateWith { mutableSetOf(it) }
}

private fun <S> addPossibleStates(
    first: Map<S, MutableSet<S>>, grammar: Grammar<S>, nullable: Set<S>
): Map<S, MutableSet<S>> {
    first.forEach { (symbol, firstForSymbol) ->
        grammar.productions.filter { it.left == symbol && it.right.isNotEmpty() }.forEach { productions ->
            for (currentSymbol in productions.right) {
                firstForSymbol.add(currentSymbol)
                if (!nullable.contains(currentSymbol)) break
            }
        }
    }
    return first
}

private fun <S> transitiveClosure(first: Map<S, MutableSet<S>>): Map<S, MutableSet<S>> {
    repeat(first.size) {
        first.forEach { (_, set) ->
            set.addAll(set.fold(mutableSetOf()) { newSet, symbol ->
                newSet.addAll(first[symbol].orEmpty())
                newSet
            })
        }
    }
    return first
}
