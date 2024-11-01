package org.exeval.parser

import org.exeval.parser.interfaces.ParseTree
import java.util.LinkedList
import java.util.Queue
import java.util.Stack

class Parser<S>(analyzedGrammar: AnalyzedGrammar<S>) {
    private val rawParser: RawParser<S, State<S>>

    init {
        val startSymbol = analyzedGrammar.grammar.startSymbol
        val endSymbol = analyzedGrammar.grammar.endOfParse
        val tableCreator = TableCreator(analyzedGrammar)
        val tables = tableCreator.getTables()

        rawParser = RawParser(startSymbol, endSymbol, tables)
    }

    fun run(leaves: List<ParseTree.Leaf<S>>): ParseTree<S> {
        return rawParser.run(leaves)
    }

    data class State<S>(val items: Set<LR1Item<S>>)
    class TableCreator<S>(private val analyzedGrammar: AnalyzedGrammar<S>) {
        private val productionMap: Map<S, Set<Production<S>>> = analyzedGrammar.grammar.productions.groupBy { prod ->
            prod.left
        }.mapValues { (_, productions) ->
            productions.toSet()
        }

        fun getTables(): Tables<S, State<S>> {
            val cc: MutableSet<State<S>> = mutableSetOf()
            val allStartingProductions = productionMap[analyzedGrammar.grammar.startSymbol]
                ?: throw IllegalStateException("No productions for start symbol")

            val cc0 =
                allStartingProductions.map { prod -> LR1Item<S>(prod, 0, analyzedGrammar.grammar.endOfParse) }.toSet()
                    .let { State(it) }.getClosure()

            val queue: Queue<State<S>> = LinkedList()
            cc.add(cc0)
            queue.add(cc0)

            val oldGoto = mutableMapOf<Pair<S, State<S>>, State<S>>()
            while (!queue.isEmpty()) {
                val cci = queue.remove()

                for (item in cci.items) {
                    val placeholderSymbol = item.production.right[item.placeholder] ?: continue
                    val newCC = getGoto(cci, placeholderSymbol)
                    cc.add(newCC)
                    queue.add(newCC)

                    oldGoto[placeholderSymbol to cci] = newCC
                }
            }

            val actions = mutableMapOf<Pair<S, State<S>>, Action<S, State<S>>>()
            val goto = mutableMapOf<Pair<S, State<S>>, State<S>>()

            for (curCC in cc) {
                for (item in curCC.items) {

                    val isAtTheEndOfProduction = item.production.right.size == item.placeholder
                    val isFromStartSymbol = item.production.left == analyzedGrammar.grammar.startSymbol
                    val isLookaheadEndOfParse = item.lookahead == analyzedGrammar.grammar.endOfParse
                    val isLookaheadTerminal = item.lookahead !in productionMap

                    when {
                        !isAtTheEndOfProduction && isLookaheadTerminal -> {
                            val placeholderSymbol = item.production.right[item.placeholder]
                            val nextCC = oldGoto[placeholderSymbol to curCC]!!
                            actions[placeholderSymbol to curCC] = Action.Shift<S, State<S>>(nextCC)
                        }

                        isAtTheEndOfProduction && isFromStartSymbol && isLookaheadEndOfParse -> {
                            actions[item.lookahead to curCC] = Action.Accept<S, State<S>>()
                        }

                        isAtTheEndOfProduction -> {
                            actions[item.lookahead to curCC] = Action.Reduce<S, State<S>>(item.production)
                        }
                    }
                }

                for (nonTerminal in productionMap.keys) {
                    goto[nonTerminal to curCC] = oldGoto[nonTerminal to curCC]!!
                }
            }

            val tables = Tables<S, State<S>>(cc0, actions, goto)
            return tables
        }

        private fun List<S>.getFIRST(): Set<S> {
            val ans = mutableSetOf<S>()
            for ((i, s) in this.withIndex()) {
                val firstForS = analyzedGrammar.firstProduct[s]
                    ?: throw IllegalStateException("There is no FIRST($s) in firstProduct")
                ans.addAll(firstForS)

                if (!analyzedGrammar.nullable.contains(s)) break
                if (i == this.size - 1) ans.add(analyzedGrammar.grammar.endOfParse)
            }

            return ans
        }

        private fun State<S>.getClosure(
        ): State<S> {
            val queue: Queue<LR1Item<S>> = LinkedList(this.items.toList())
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
                        resItems.add(LR1Item<S>(production, 0, terminal))
                        queue.add(LR1Item<S>(production, 0, terminal))
                    }
                }
            }

            return State(resItems)
        }

        private fun getGoto(state: State<S>, symbol: S): State<S> {
            val res = mutableSetOf<LR1Item<S>>()
            for (item in state.items) {
                // checking if we have wrong symbol or are at the end of production
                if (item.production.right.getOrNull(item.placeholder) != symbol) continue

                res.add(item.copy(placeholder = item.placeholder + 1))
            }

            return State(res).getClosure()
        }

    }

    data class LR1Item<S>(val production: Production<S>, val placeholder: Int, val lookahead: S)

}

class RawParser<Symbol, State>(
    private val startSymbol: Symbol, private val endSymbol: Symbol, private val tables: Tables<Symbol, State>
) {

    private inner class StackEntry(val tree: ParseTree<Symbol>, val state: State)

    private infix fun ParseTree<Symbol>.to(state: State): StackEntry = StackEntry(this, state)


    fun run(leaves: List<ParseTree.Leaf<Symbol>>): ParseTree<Symbol> {
        val stack = Stack<StackEntry>()
        stack.push(
            ParseTree.Leaf<Symbol>(
                startSymbol, leaves.first().startLocation, leaves.last().endLocation
            ) to tables.startState
        )
        var leafI = 0

        while (true) {
            val state: State = stack.peek().state
            val leaf: ParseTree.Leaf<Symbol> = leaves[leafI]

            val curAction = tables.actions[leaf.symbol to state]
            if (curAction is Action.Reduce<Symbol, State>) {
                val prod = curAction.production
                val newBranch = takeProductionFromStack(prod, stack)
                val newState = tables.goto[prod.left to stack.peek().state]
                    ?: throw ParseError("Goto is incomplete, no goto for (%s to %s)")
                stack.push(newBranch to newState)
            } else if (curAction is Action.Shift<Symbol, State>) {
                val newState = curAction.state
                stack.push(leaf to newState)
                leafI += 1
            } else if (curAction is Action.Accept<Symbol, State> && leaf.symbol == endSymbol) {
                return stack.peek().tree
            } else {
                throw ParseError("Parse error at leaf %s".format(leaf))
            }
        }

    }

    private fun takeProductionFromStack(
        prod: Production<Symbol>, stack: Stack<StackEntry>
    ): ParseTree.Branch<Symbol> {
        val children = mutableListOf<ParseTree<Symbol>>()
        repeat(prod.right.size) { children.add(stack.pop().tree) }
        children.reverse()
        val newBranch = ParseTree.Branch(prod, children, children.first().startLocation, children.last().endLocation)

        return newBranch
    }
}

data class Tables<Symbol, State>(
    val startState: State,
    val actions: Map<Pair<Symbol, State>, Action<Symbol, State>>,
    val goto: Map<Pair<Symbol, State>, State>
)

sealed interface Action<Symbol, State> {
    data class Reduce<Symbol, State>(val production: Production<Symbol>) : Action<Symbol, State>
    data class Shift<Symbol, State>(val state: State) : Action<Symbol, State>
    class Accept<Symbol, State> : Action<Symbol, State>
}

class ParseError(override val message: String) : Exception()