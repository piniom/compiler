package org.exeval.parser.parser.impls

import org.exeval.parser.Production
import org.exeval.parser.interfaces.ParseTree
import org.exeval.parser.parser.OneTimeParser
import org.exeval.parser.parser.ParseError
import org.exeval.parser.parser.TablesCreator
import org.exeval.parser.parser.TablesCreator.Action
import java.util.*

class LR1OneTimeParser<Symbol, State>(
    private val leaves: List<ParseTree.Leaf<Symbol>>,
    private val startSymbol: Symbol,
    private val endSymbol: Symbol,
    private val tables: TablesCreator.Tables<Symbol, State>,
) : OneTimeParser<Symbol> {
    private inner class StackEntry(val tree: ParseTree<Symbol>, val state: State)

    private infix fun ParseTree<Symbol>.to(state: State): StackEntry = StackEntry(this, state)

    private val stack = Stack<StackEntry>()
    private var leafI = 0
    private val leaf: ParseTree.Leaf<Symbol>
        get() = leaves[leafI]

    override fun run(): ParseTree.Branch<Symbol> {
        leafI = 0
        stack.clear()

        stack.push(
            ParseTree.Leaf(
                startSymbol, leaves.first().startLocation, leaves.last().endLocation
            ) to tables.startState
        )

        while (true) {
            val state: State = stack.peek().state

            val curAction = tables.actions[leaf.symbol to state]
            if (curAction is Action.Reduce<Symbol, State>) {
                val prod = curAction.production
                val newBranch = takeProductionFromStack(prod)
                val newState = tables.goto[prod.left to stack.peek().state] ?: throw ParseError(
                    "Goto is incomplete. Perhaps there is an error in constructor. No goto for (%s to %s)".format(
                        prod.left, stack.peek().state
                    ), newBranch.startLocation, newBranch.endLocation
                )
                stack.push(newBranch to newState)
            } else if (curAction is Action.Shift<Symbol, State>) {
                val newState = curAction.state
                stack.push(leaf to newState)
                leafI += 1
            } else if (curAction is Action.Accept<Symbol, State> && leaf.symbol == endSymbol) {
                val curPeekTree = stack.peek().tree
                if (curPeekTree !is ParseTree.Branch<Symbol>) throw IllegalStateException("Tried to return Leaf. That can only happen, if there is production Start -> Terminal.")
                return curPeekTree
            } else {
                throw ParseError("Parse error at leaf %s".format(leaf), leaf.startLocation, leaf.endLocation)
            }
        }

    }

    private fun takeProductionFromStack(
        prod: Production<Symbol>
    ): ParseTree.Branch<Symbol> {
        val children = mutableListOf<ParseTree<Symbol>>()
        repeat(prod.right.size) { children.add(stack.pop().tree) }
        children.reverse()
        val newBranch = ParseTree.Branch(prod, children, children.first().startLocation, children.last().endLocation)

        return newBranch
    }

    class Factory<Symbol, State>(
        private val startSymbol: Symbol,
        private val endSymbol: Symbol,
        private val tables: TablesCreator.Tables<Symbol, State>
    ) : OneTimeParser.Factory<Symbol> {
        override fun create(leaves: List<ParseTree.Leaf<Symbol>>): OneTimeParser<Symbol> {
            return LR1OneTimeParser(leaves, startSymbol, endSymbol, tables)
        }
    }
}