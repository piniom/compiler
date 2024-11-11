package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.CFGNodeImpl
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager

class FunctionFrameManagerImpl(override val f: FunctionDeclaration, override val analyser: FunctionAnalysisResult) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private val registerPool = mutableListOf<UsableMemoryCell.VirtReg>()
    private var stackOffset = 0

    private val RAX = PhysicalRegister(0)
    private val RSP = PhysicalRegister(4)
    private val RBP = PhysicalRegister(5)

    companion object {
        const val DEFAULT_REGISTER_COUNT = 16
    }

    init {
        initRegisterPool(DEFAULT_REGISTER_COUNT)
    }


    override fun generate_var_access(x: AnyVariable): Tree {
        TODO("Not yet implemented")
    }

    override fun generate_function_call(trees: List<Tree>, then: CFGNode): CFGNode {
        TODO("Not yet implemented")
    }

    override fun variable_to_virtual_register(x: AnyVariable): UsableMemoryCell {
        variableMap[x]?.let { return it }

        val cell: UsableMemoryCell;
        if (registerPool.isNotEmpty()) {
            cell = registerPool.removeAt(0)
        }
        else {
            val offset = stackOffset
            stackOffset += 4
            cell = UsableMemoryCell.MemoryPlace(offset)
        }

        variableMap[x] = cell
        return cell
    }

    override fun generate_prolog(then: CFGNode): CFGNode {
        val trees = mutableListOf<Tree>()

        trees.addAll(pushToStack(RBP))
        trees.add(Assigment(RBP, RSP))


        for (parameter in f.parameters) {
            trees.add(generate_var_access(parameter))
        }

        return CFGNodeImpl(then, null, trees.toList())
    }

    override fun generate_epilouge(result: Tree?): CFGNode {
        val trees = mutableListOf<Tree>()

        result?.let {
            trees.add(Assigment(RAX, it))
        }

        trees.add(Assigment(RBP, RSP))
        trees.addAll(popFromStack(RBP))
        trees.add(Return)

        return CFGNodeImpl(null, null, trees.toList())
    }

    private fun initRegisterPool(registerCount: Int) {
        for (idx in 0 until registerCount) {
            registerPool.add(UsableMemoryCell.VirtReg(idx))
        }
    }

    private fun pushToStack(reg: Register): List<Tree> {
        val size = 8
        return listOf(
            BinaryOperation(RSP, Constant(size), BinaryOperationType.SUBTRACT),
            Assigment(RSP, reg)
        )
    }

    private fun popFromStack(reg: Register): List<Tree> {
        val size = 8
        return listOf(
            Assigment(reg, RSP),
            BinaryOperation(RSP, Constant(size), BinaryOperationType.ADD)
        )
    }

}