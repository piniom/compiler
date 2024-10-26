package org.exeval.parser

import org.exeval.parser.interfaces.ParseTree
import java.util.Stack

class Parser<S> {
//    private val rawParser: RawParser<S, *>

    constructor(grammar: AnalyzedGrammar<S>) {
        //...

//        TODO("Define state, actions and goto")
//        val startSymbol: S = grammar.grammar.startSymbol
//        val endSymbol: S = grammar.grammar.endOfParse
//        val startingState: State
//        val actions: Map<Pair<Symbol, State>, Action<Symbol>>,
//        val goto: Map<Pair<Symbol, State>, Symbol>
//        rawParser = RawParser<Symbol, Any>(startSymbol, endSymbol, startingState, actions, goto)
    }

    fun run(leaves: List<ParseTree.Leaf<S>>): ParseTree<S> {
        TODO("Not implemented")
//        return rawParser.run(leaves)
    }
}


class RawParser<Symbol, State>(
    private val startSymbol: Symbol,
    private val endSymbol: Symbol,
    private val startState: State,
    private val actions: Map<Pair<Symbol, State>, Action<Symbol, State>>,
    private val goto: Map<Pair<Symbol, State>, State>
) {
    private inner class StackEntry(val tree: ParseTree<Symbol>, val state: State)

    private infix fun ParseTree<Symbol>.to(state: State): StackEntry = StackEntry(this, state)

    fun run(leaves: List<ParseTree.Leaf<Symbol>>): ParseTree<Symbol> {
        val stack = Stack<StackEntry>()
        stack.push(
            ParseTree.Leaf<Symbol>(
                startSymbol, leaves.first().startLocation, leaves.last().endLocation
            ) to startState
        )
        var leafI = 0

        while (true) {
            val state: State = stack.peek().state
            val leaf: ParseTree.Leaf<Symbol> = leaves[leafI]

            val curAction = actions[leaf.symbol to state]
            if (curAction is Action.Reduce) {
                val prod = curAction.production
                val newBranch = takeProductionFromStack(prod, stack)
                val newState = goto[prod.left to stack.peek().state]
                    ?: throw ParseError("Goto is incomplete, no goto for (%s to %s)")
                stack.push(newBranch to newState)
            } else if (curAction is Action.Shift) {
                val newState = curAction.state
                stack.push(leaf to newState)
                leafI += 1
            } else if (curAction is Action.Accept && leaf.symbol == endSymbol) {
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

sealed interface Action<Symbol, State> {
    data class Reduce<Symbol, State>(val production: Production<Symbol>) : Action<Symbol, State>
    data class Shift<Symbol, State>(val state: State) : Action<Symbol, State>
    class Accept<Symbol, State> : Action<Symbol, State>
}

class ParseError(override val message: String) : Exception()