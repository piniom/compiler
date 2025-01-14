package org.exeval.parser.parser.impls

import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Production
import org.exeval.parser.parser.TablesCreator
import org.exeval.parser.parser.TablesCreator.Action
import org.exeval.parser.parser.TablesCreator.Tables
import java.util.*

class BigTablesCreator<S>(private val analyzedGrammar: AnalyzedGrammar<S>) : TablesCreator<S, RealState<S>> {
    private val productionMap: Map<S, Set<Production<S>>> = analyzedGrammar.grammar.productions.groupBy { prod ->
        prod.left
    }.mapValues { (_, productions) ->
        productions.toSet()
    }
    override val tables: Tables<S, RealState<S>>

    init {
        val startingState = getStartingState()

        val statesGotoPair = getAllStatesAndOldGoto(startingState)
        val allStates: Set<RealState<S>> = statesGotoPair.first
        val oldGoto: Map<Pair<S, RealState<S>>, RealState<S>> = statesGotoPair.second

        val actions = getActions(allStates, oldGoto)
        val tableGoto = getTableGoto(allStates, oldGoto)

        tables = Tables(startingState, actions, tableGoto)
    }

    private fun getTableGoto(
        allStates: Set<RealState<S>>, oldGoto: Map<Pair<S, RealState<S>>, RealState<S>>
    ): Map<Pair<S, RealState<S>>, RealState<S>> {
        val tableGoto = mutableMapOf<Pair<S, RealState<S>>, RealState<S>>()
        for (curCC in allStates) {
            for (nonTerminal in productionMap.keys) {
                val oldGotoState = oldGoto[nonTerminal to curCC] ?: continue
                if (oldGotoState.items.isEmpty()) continue
                tableGoto[nonTerminal to curCC] = oldGotoState
            }
        }

        return tableGoto
    }

    private fun getActions(
        allStates: Set<RealState<S>>, oldGoto: Map<Pair<S, RealState<S>>, RealState<S>>
    ): Map<Pair<S, RealState<S>>, Action<S, RealState<S>>> {
        val actions = mutableMapOf<Pair<S, RealState<S>>, Action<S, RealState<S>>>()

        for (curCC in allStates) {
            for (item in curCC.items) {
                val isAtTheEndOfProduction = item.production.right.size == item.placeholder
                val isFromStartSymbol = item.production.left == analyzedGrammar.grammar.startSymbol
                val isLookaheadEndOfParse = item.lookahead == analyzedGrammar.grammar.endOfParse
                val isLookaheadTerminal = item.lookahead !in productionMap

                when {
                    !isAtTheEndOfProduction -> {
                        val placeholderSymbol = item.production.right[item.placeholder]
                        if (placeholderSymbol in productionMap) continue

                        val nextCC = oldGoto[placeholderSymbol to curCC]!!
                        val oldVal = actions[placeholderSymbol to curCC]
                        val newVal = Action.Shift<S, RealState<S>>(nextCC)

                        if (oldVal != null && oldVal != newVal && oldVal !is Action.Reduce) {
                            throw TableCreationError("There is conflict in an action table. Grammar is probably ambiguous. Entry actions[${placeholderSymbol}, $curCC] have two values: $oldVal and $newVal")
                        }
                        actions[placeholderSymbol to curCC] = newVal
                    }

                    isAtTheEndOfProduction && isFromStartSymbol && isLookaheadEndOfParse -> {
                        actions[item.lookahead to curCC] = Action.Accept()
                    }

                    isAtTheEndOfProduction && isLookaheadTerminal -> {
                        val oldVal = actions[item.lookahead to curCC]
                        val newVal = Action.Reduce<S, RealState<S>>(item.production)

                        if (oldVal != null && oldVal is Action.Shift<*, *>) {
                            continue
                        } else if (oldVal != null && oldVal != newVal) {
                            throw TableCreationError("There is conflict in an action table. Grammar is probably ambiguous. Entry actions[${item.lookahead}, $curCC] have two values: $oldVal and $newVal")
                        } else {
                            actions[item.lookahead to curCC] = newVal
                        }
                    }
                }
            }
        }
        return actions
    }

    private fun getStartingState(): RealState<S> {
        val allStartingProductions = productionMap[analyzedGrammar.grammar.startSymbol]
            ?: throw IllegalStateException("No productions for start symbol")

        val startingState =
            allStartingProductions.map { prod -> RealState.LR1Item(prod, 0, analyzedGrammar.grammar.endOfParse) }
                .toSet().let { RealState(it) }.getClosure()

        return startingState
    }

    private fun getAllStatesAndOldGoto(startingState: RealState<S>): Pair<Set<RealState<S>>, Map<Pair<S, RealState<S>>, RealState<S>>> {
        val allStates: MutableSet<RealState<S>> = mutableSetOf()
        val oldGoto = mutableMapOf<Pair<S, RealState<S>>, RealState<S>>()

        val queue: Queue<RealState<S>> = LinkedList()
        allStates.add(startingState)
        queue.add(startingState)

        while (!queue.isEmpty()) {
            val cci = queue.remove()

            for (item in cci.items) {
                val placeholderSymbol = item.production.right.getOrNull(item.placeholder) ?: continue
                val newCC = getGoto(cci, placeholderSymbol)
                if (allStates.add(newCC)) queue.add(newCC)

                oldGoto[placeholderSymbol to cci] = newCC
            }
        }

        return allStates to oldGoto
    }

    private fun List<S>.getFIRST(): Set<S> {
        val ans = mutableSetOf<S>()
        for ((i, s) in this.withIndex()) {
            val firstForS = if (s in productionMap) analyzedGrammar.firstProduct[s]
                ?: throw IllegalStateException("There is no FIRST($s) in firstProduct")
            else listOf(s)
            ans.addAll(firstForS)

            if (!analyzedGrammar.nullable.contains(s)) break
            if (i == this.size - 1) ans.add(analyzedGrammar.grammar.endOfParse)
        }

        return ans
    }

    private fun RealState<S>.getClosure(
    ): RealState<S> {
        val queue: Queue<RealState.LR1Item<S>> = LinkedList(this.items.toList())
        val resItems = this.items.toMutableSet()
        while (queue.isNotEmpty()) {
            val item = queue.remove()
            val placeholder = item.placeholder

            // checking if we are at the end of production
            val placeholderSymbol = item.production.right.getOrNull(placeholder) ?: continue
            // that means curSymbol is terminal, so we don't care
            val productionsForSymbol = productionMap[placeholderSymbol] ?: continue

            val lookaheadString = item.production.right.subList(
                item.placeholder + 1, item.production.right.size
            ) + listOf(item.lookahead)

            for (production in productionsForSymbol) {
                for (terminal in lookaheadString.getFIRST()) {
                    if (resItems.add(
                            RealState.LR1Item(
                                production, 0, terminal
                            )
                        )
                    ) queue.add(RealState.LR1Item(production, 0, terminal))
                }
            }
        }

        return RealState(resItems)
    }

    private fun getGoto(state: RealState<S>, symbol: S): RealState<S> {
        val res = mutableSetOf<RealState.LR1Item<S>>()
        for (item in state.items) {
            // checking if we have wrong symbol or are at the end of production
            if (item.production.right.getOrNull(item.placeholder) != symbol) continue

            res.add(item.copy(placeholder = item.placeholder + 1))
        }

        return RealState(res).getClosure()
    }
}

class TableCreationError(override val message: String) : Exception()