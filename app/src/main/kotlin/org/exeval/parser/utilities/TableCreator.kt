package org.exeval.parser.utilities

import org.exeval.parser.AnalyzedGrammar
import org.exeval.parser.Parser
import org.exeval.parser.Parser.Action
import org.exeval.parser.Parser.Tables
import org.exeval.parser.Production
import java.util.LinkedList
import java.util.Queue

class TableCreator<S>(private val analyzedGrammar: AnalyzedGrammar<S>) {
    private val productionMap: Map<S, Set<Production<S>>> = analyzedGrammar.grammar.productions.groupBy { prod ->
        prod.left
    }.mapValues { (_, productions) ->
        productions.toSet()
    }
    val tables: Tables<S, Parser.State<S>>

    init {
        val startingState = getStartingState()

        val statesGotoPair = getAllStatesAndOldGoto(startingState)
        val allStates: Set<Parser.State<S>> = statesGotoPair.first
        val oldGoto: Map<Pair<S, Parser.State<S>>, Parser.State<S>> = statesGotoPair.second

        val actions = getActions(allStates, oldGoto)
        val tableGoto = getTableGoto(allStates, oldGoto)

        tables = Tables<S, Parser.State<S>>(startingState, actions, tableGoto)
    }

    private fun getTableGoto(
        allStates: Set<Parser.State<S>>, oldGoto: Map<Pair<S, Parser.State<S>>, Parser.State<S>>
    ): Map<Pair<S, Parser.State<S>>, Parser.State<S>> {
        val tableGoto = mutableMapOf<Pair<S, Parser.State<S>>, Parser.State<S>>()
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
        allStates: Set<Parser.State<S>>, oldGoto: Map<Pair<S, Parser.State<S>>, Parser.State<S>>
    ): Map<Pair<S, Parser.State<S>>, Action<S, Parser.State<S>>> {
        val actions = mutableMapOf<Pair<S, Parser.State<S>>, Action<S, Parser.State<S>>>()

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
                        val newVal = Action.Shift<S, Parser.State<S>>(nextCC)

                        if (oldVal != null && oldVal != newVal && oldVal !is Action.Reduce) {
                            throw TableCreationError("There is conflict in an action table. Grammar is probably ambiguous. Entry actions[${placeholderSymbol}, $curCC] have two values: $oldVal and $newVal")
                        }
                        actions[placeholderSymbol to curCC] = newVal
                    }

                    isAtTheEndOfProduction && isFromStartSymbol && isLookaheadEndOfParse -> {
                        actions[item.lookahead to curCC] = Action.Accept<S, Parser.State<S>>()
                    }

                    isAtTheEndOfProduction && isLookaheadTerminal -> {
                        val oldVal = actions[item.lookahead to curCC]
                        val newVal = Action.Reduce<S, Parser.State<S>>(item.production)

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

    private fun getStartingState(): Parser.State<S> {
        val allStartingProductions = productionMap[analyzedGrammar.grammar.startSymbol]
            ?: throw IllegalStateException("No productions for start symbol")

        val startingState =
            allStartingProductions.map { prod -> Parser.LR1Item<S>(prod, 0, analyzedGrammar.grammar.endOfParse) }
                .toSet().let { Parser.State(it) }.getClosure()

        return startingState
    }

    private fun getAllStatesAndOldGoto(startingState: Parser.State<S>): Pair<Set<Parser.State<S>>, Map<Pair<S, Parser.State<S>>, Parser.State<S>>> {
        val allStates: MutableSet<Parser.State<S>> = mutableSetOf()
        val oldGoto = mutableMapOf<Pair<S, Parser.State<S>>, Parser.State<S>>()

        val queue: Queue<Parser.State<S>> = LinkedList()
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

    private fun Parser.State<S>.getClosure(
    ): Parser.State<S> {
        val queue: Queue<Parser.LR1Item<S>> = LinkedList(this.items.toList())
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
                            Parser.LR1Item<S>(
                                production, 0, terminal
                            )
                        )
                    ) queue.add(Parser.LR1Item<S>(production, 0, terminal))
                }
            }
        }

        return Parser.State(resItems)
    }

    private fun getGoto(state: Parser.State<S>, symbol: S): Parser.State<S> {
        val res = mutableSetOf<Parser.LR1Item<S>>()
        for (item in state.items) {
            // checking if we have wrong symbol or are at the end of production
            if (item.production.right.getOrNull(item.placeholder) != symbol) continue

            res.add(item.copy(placeholder = item.placeholder + 1))
        }

        return Parser.State(res).getClosure()
    }
}

class TableCreationError(override val message: String) : Exception()