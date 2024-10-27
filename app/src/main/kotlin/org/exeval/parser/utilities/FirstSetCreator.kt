package org.exeval.parser.utilities

import org.exeval.parser.Grammar

fun <S> createFirstSet(grammar: Grammar<S>, nullable: Set<S>): Map<S, List<S>> {
    return transitiveClosure(
        addPossibleStates(
            crateMapWithOnlyKeySymbol(grammar), grammar, nullable
        )
    ).mapValues { it.value.distinct() }
}

private fun <S> crateMapWithOnlyKeySymbol(grammar: Grammar<S>): Map<S, MutableList<S>> {
    return grammar.productions.map { it.left }.associateWith { mutableListOf(it) }
}

private fun <S> addPossibleStates(
    first: Map<S, MutableList<S>>, grammar: Grammar<S>, nullable: Set<S>
): Map<S, MutableList<S>> {
    first.forEach { (symbol, firstForSymbol) ->
        grammar.productions.filter { it.left == symbol && it.right.isNotEmpty() }.forEach { productions ->
            for (currentSymbol in productions.right) {
                if (!firstForSymbol.contains(currentSymbol) )
                    firstForSymbol.add(currentSymbol)
                if (!nullable.contains(currentSymbol)) break
            }
        }
    }
    return first
}

private fun <S> transitiveClosure(first: Map<S, MutableList<S>>): Map<S, MutableList<S>> {
    repeat(first.size) {
        first.forEach { (_, list) ->
            list.addAll(list.fold(mutableListOf()) { newList, symbol ->
                newList.addAll(first[symbol].orEmpty())
                newList
            })
        }
    }
    return first
}
