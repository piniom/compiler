package org.exeval.parser.utilities

import org.exeval.parser.ParseError
import org.exeval.parser.Parser.Action
import org.exeval.parser.Parser.Tables
import org.exeval.parser.Production
import org.exeval.parser.interfaces.ParseTree
import java.util.Stack

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
                    ?: throw ParseError("Goto is incomplete, no goto for (%s to %s)".format(prod.left, stack.peek().state))
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