package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager

class FunctionFrameManagerImpl(override val f: FunctionDeclaration) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private val registerPool = mutableListOf<UsableMemoryCell.VirtReg>()
    private var stackOffset = 0

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
        TODO("Not yet implemented")
    }

    override fun generate_epilouge(result: Tree?): CFGNode {
        TODO("Not yet implemented")
    }

    private fun initRegisterPool(registerCount: Int) {
        for (idx in 0 until registerCount) {
            registerPool.add(UsableMemoryCell.VirtReg(idx))
        }
    }
}