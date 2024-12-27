package org.exeval.cfg

import org.exeval.cfg.ffm.interfaces.CallManager
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.ast.ForeignFunctionDeclaration
import org.exeval.cfg.Tree
import org.exeval.cfg.PhysicalRegister

class ForeignCallManager(private val function: ForeignFunctionDeclaration) : CallManager {

    /*
    Convention
    First argument: rdi
    Second argument: rsi
    Third argument: rdx
    Fourth argument: rcx
    Fifth argument: r8
    Sixth argument: r9
    Rest goes on stack
    */

    private val argumentNumToRegister : Map<Int, PhysicalRegister> = mapOf(
        0 to PhysicalRegister.RDI,
        1 to PhysicalRegister.RSI,
        2 to PhysicalRegister.RDX,
        3 to PhysicalRegister.RCX,
        4 to PhysicalRegister.R8,
        5 to PhysicalRegister.R9
    )
    private val numFromArgsGoesToStack = 6

    val functionLabel = Label(function.name)

    override fun generate_function_call(trees: List<Tree>, result: AssignableTree?, then: CFGNode): CFGNode {

        val outTrees = mutableListOf<Tree>()
        for (i in 0 until minOf(numFromArgsGoesToStack, trees.size)) {
            outTrees.add(
                AssignmentTree(
                    RegisterTree(argumentNumToRegister[i]!!),
                    trees[i]
                )
            )
        }

        // Put the rest of the args on stack
        for (i in numFromArgsGoesToStack..<trees.size) {
            outTrees.addAll(
                pushToStack(trees[i])
            )
        }

        // Add Call instruction
        outTrees.add(Call(functionLabel))

        // Store result from RAX if needed
        result?.let {
            outTrees.add(
                AssignmentTree(
                    it,
                    RegisterTree(PhysicalRegister.RAX)
                )
            )
        }

        return CFGNodeImpl(
            Pair(then, null),
            outTrees
        )
    }

    private fun pushToStack(tree: Tree): List<Tree> {
        return listOf(
            BinaryOperationTree(
                RegisterTree(PhysicalRegister.RSP),
                NumericalConstantTree(Register.SIZE),
                BinaryTreeOperationType.SUBTRACT
            ),
            AssignmentTree(MemoryTree(RegisterTree(PhysicalRegister.RSP)), tree)
        )
    }
}