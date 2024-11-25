package org.exeval.cfg

import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.Assignment as CFGAssignment
import org.exeval.cfg.BinaryOperation as BinaryOp
import org.exeval.cfg.BinaryOperationType as BinaryOpType
import org.exeval.ast.*
import org.exeval.ast.NameResolution
import org.exeval.cfg.BinaryOperationType
import org.exeval.ffm.interfaces.FunctionFrameManager

class Node(override var branches: Pair<CFGNode, CFGNode?>?, override var trees: List<Tree>) : CFGNode {
    constructor() : this(null, listOf())
    constructor(branch: CFGNode) : this(Pair(branch, null), listOf())
    constructor(branches: Pair<CFGNode, CFGNode>) : this(branches, listOf())
    constructor(branch: CFGNode, tree: Tree) : this(Pair(branch, null), listOf(tree))
}


class WalkResult(val top: CFGNode, public var tree: Tree?)

class CFGMaker(
    private val fm: FunctionFrameManager,
    private val nameResolution: NameResolution,
    private val varUsage: VariableUsageAnalysisResult,
    private val typeMap: TypeMap
) {

    private val loopToNode: MutableMap<Loop, Pair<CFGNode, Assignable?>> = mutableMapOf()

    public fun makeCfg(ast: FunctionDeclaration): CFGNode {
        val node = Node()
        val body = walkExpr(ast.body, node)
        val bottom = fm.generate_epilouge(body.tree)
        node.branches = Pair(bottom, null)
        return fm.generate_prolog(body.top)
    }

    private fun walkExpr(expr: Expr?, then: CFGNode): WalkResult {
        return when (expr) {
            is Assignment -> walkAssignment(expr, then)
            is BinaryOperation -> walkBinaryOperation(expr, then)
            is Block -> walkBlock(expr, then)
            is Break -> walkBreak(expr, then)
            is Conditional -> walkConditional(expr, then)
            is FunctionCall -> walkFunctionCall(expr, then)
            is FunctionDeclaration -> WalkResult(then, null)
            is Literal -> walkLiteral(expr, then)
            is Loop -> walkLoop(expr, then)
            is UnaryOperation -> walkUnaryOperation(expr, then)
            is VariableDeclarationBase -> walkVariableDeclarationBase(expr, then)
            is VariableReference -> walkVariableReference(expr, then)
            null -> WalkResult(then, null)
        }
    }


    private fun walkAssignment(assignment: Assignment, then: CFGNode): WalkResult {
        val node = Node(then)
        val value = walkExpr(assignment.value, node)
        val variable = nameResolution.variableToDecl[VariableReference(assignment.variable)]!!
        val access = fm.generate_var_access(variable)
        node.trees = listOf(
            if (typeMap[assignment.value]!!.isNope()) {
                value.tree!!
            } else {
                CFGAssignment(access, value.tree!!)
            }
        )
        return WalkResult(value.top, null)
    }

    private fun walkBinaryOperation(bin: BinaryOperation, then: CFGNode): WalkResult {
        val leftUsage = varUsage[bin.left]!!
        val rightUsage = varUsage[bin.right]!!
        if (leftUsage.conflicts(rightUsage)) {
            val r = walkExpr(bin.right, then)
            val node = Node(r.top)
            val l = walkExpr(bin.left, node)
            val reg = newVirtualRegister()
            node.trees = listOf(CFGAssignment(reg, l.tree!!))
            return WalkResult(
                l.top,
                BinaryOp(reg, r.tree!!, convertBinOp(bin.operator))
            )
        } else {
            val r = walkExpr(bin.right, then)
            val l = walkExpr(bin.left, r.top)
            return WalkResult(
                l.top,
                BinaryOp(l.tree!!, r.tree!!, convertBinOp(bin.operator))
            )
        }
    }

    private fun walkBlock(block: Block, then: CFGNode): WalkResult {
        val reversed = block.expressions.reversed()
        var prev = WalkResult(then, null)
        for (e in reversed) {
            val node = if (prev.tree != null) {
                Node(prev.top, prev.tree!!)
            } else {
                prev.top
            }
            prev = walkExpr(e, node)
        }
        return prev
    }

    private fun walkBreak(breakk: Break, then: CFGNode): WalkResult {
        val loop = nameResolution.breakToLoop[breakk]!!
        val pair = loopToNode[loop]!!
        val node = Node(pair.first)
        if (typeMap[loop]!!.isNope()) {
            return WalkResult(node, null)
        }
        val result = walkExpr(breakk.expression!!, node)
        node.trees = listOf(CFGAssignment(pair.second!!, result.tree!!))
        return WalkResult(result.top, null)
    }

    private fun walkConditional(conditional: Conditional, then: CFGNode): WalkResult {
        if (typeMap[conditional]!!.isNope()) {
            return walkNopeConditional(conditional, then)
        }

        val reg = newVirtualRegister()

        val saveElse = Node(then)
        val elseBranch = walkExpr(conditional.elseBranch, saveElse)
        saveElse.trees = listOf(CFGAssignment(reg, elseBranch.tree!!))

        val saveThen = Node(then)
        val thenBranch = walkExpr(conditional.thenBranch, saveThen)
        saveThen.trees = listOf(CFGAssignment(reg, thenBranch.tree!!))

        val node = Node(Pair(thenBranch.top, elseBranch.top))
        val condition = walkExpr(conditional.condition, node)
        node.trees = listOf(condition.tree!!)

        return WalkResult(
            condition.top,
            reg
        )
    }

    private fun walkNopeConditional(conditional: Conditional, then: CFGNode): WalkResult {
        val elseBranch = walkExpr(conditional.elseBranch, then)
        val thenBranch = walkExpr(conditional.thenBranch, then)
        val node = Node(Pair(thenBranch.top, elseBranch.top))
        val condition = walkExpr(conditional.condition, node)
        node.trees = listOf(condition.tree!!)
        return WalkResult(condition.top, null)
    }

    private fun walkFunctionCall(functionCall: FunctionCall, then: CFGNode): WalkResult {
        val declaration = nameResolution.functionToDecl[functionCall]!!
        val arguments = functionCall.arguments.map {
            Pair(
                it,
                declaration.parameters.indexOf(nameResolution.argumentToParam[it]!!)
            )
        }.sortedByDescending { it.second }.map { it.first }

        val trees: MutableList<Tree> = mutableListOf()
        val node = Node()
        var prev: CFGNode = node

        val conflictingSet: MutableSet<VariableUsage> = mutableSetOf()

        for (a in arguments) {
            val expression = when (a) {
                is NamedArgument -> a.expression
                is PositionalArgument -> a.expression
            }
            val curUsage = varUsage[expression]!!

            if (typeMap[expression]!!.isNope()) {
                // Create an additional block so that the Nope expression is evaluated
                // It may have side effects
                // Don't push it to the trees
                val temp = Node(prev)
                val result = walkExpr(expression, temp)
                node.trees = listOfNotNull(result.tree)
                prev = result.top
            } else if (conflictingSet.any { it.conflicts(curUsage) }) {
                // We need to create an additional node and save the result since later nodes may destroy it
                val temp = Node(prev)
                val result = walkExpr(expression, temp)
                val reg = newVirtualRegister()
                node.trees = listOf(CFGAssignment(reg, result.tree!!))
                prev = result.top
                trees.addFirst(reg)
            } else {
                // No need to create an additional node since it will be evalueted when the value is passed
                val result = walkExpr(expression, prev)
                prev = result.top
                trees.addFirst(result.tree!!)
            }
            conflictingSet.add(curUsage)
        }

        val reg = if (typeMap[functionCall]!!.isNope()) null else newVirtualRegister()
        val call = fm.generate_function_call(trees, reg, then)
        node.branches = Pair(call, null)
        return WalkResult(prev, reg)
    }

    private fun walkLoop(loop: Loop, then: CFGNode): WalkResult {
        val reg = if (typeMap[loop]!!.isNope()) null else newVirtualRegister()
        loopToNode[loop] = Pair(then, reg)

        val start = Node()
        val inner = walkExpr(loop.body, start).top
        start.branches = Pair(inner, null)

        return WalkResult(
            start,
            reg
        )
    }

    private fun walkLiteral(literal: Literal, then: CFGNode): WalkResult {
        return when (literal) {
            is IntLiteral -> WalkResult(then, Constant(literal.value))
            is BoolLiteral -> WalkResult(then, Constant(if (literal.value) 1 else 0))
            NopeLiteral -> WalkResult(then, null)
        }
    }

    private fun walkUnaryOperation(unaryOperation: UnaryOperation, then: CFGNode): WalkResult {
        val inner = walkExpr(unaryOperation.operand, then)
        return WalkResult(inner.top, UnaryOp(inner.tree!!, convertUnOp(unaryOperation.operator)))
    }

    private fun walkVariableDeclarationBase(variableDeclaration: VariableDeclarationBase, then: CFGNode): WalkResult {
        if (variableDeclaration.initializer == null || typeMap[variableDeclaration.initializer!!]!!.isNope()) return WalkResult(
            then,
            null
        )
        val node = Node(then)
        val inner = walkExpr(variableDeclaration.initializer!!, node)
        val destination = fm.generate_var_access(variableDeclaration)
        node.trees = listOf(CFGAssignment(destination, inner.tree!!))
        return WalkResult(inner.top, null)
    }

    private fun walkVariableReference(variableReference: VariableReference, then: CFGNode): WalkResult {
        if (typeMap[variableReference]!!.isNope()) return WalkResult(then, null)
        val value = fm.generate_var_access(nameResolution.variableToDecl[variableReference]!!)
        return WalkResult(then, value)
    }

    // Is there a better way to generate registers?
    private var counter = 1000;
    private fun newVirtualRegister(): VirtualRegister {
        return VirtualRegister(counter++)
    }

    private fun convertBinOp(operation: BinaryOperator): BinaryOpType {
        return when (operation) {
            BinaryOperator.PLUS -> BinaryOperationType.ADD
            BinaryOperator.MINUS -> BinaryOperationType.SUBTRACT
            BinaryOperator.MULTIPLY -> BinaryOperationType.MULTIPLY
            BinaryOperator.DIVIDE -> BinaryOperationType.DIVIDE
            BinaryOperator.AND -> BinaryOperationType.AND
            BinaryOperator.OR -> BinaryOperationType.OR
            BinaryOperator.EQ -> BinaryOperationType.EQUAL
            BinaryOperator.GT -> BinaryOperationType.GREATER
            BinaryOperator.GTE -> BinaryOperationType.GREATER_EQUAL
            BinaryOperator.LT -> BinaryOperationType.LESS
            BinaryOperator.LTE -> BinaryOperationType.LESS_EQUAL
        }
    }

    private fun convertUnOp(operation: UnaryOperator): UnaryOperationType {
        return when (operation) {
            UnaryOperator.NOT -> UnaryOperationType.NOT
            UnaryOperator.MINUS -> UnaryOperationType.MINUS
        }
    }
}