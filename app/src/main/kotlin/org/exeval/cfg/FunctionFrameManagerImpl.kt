package org.exeval.cfg

import com.sun.source.tree.ConstantCaseLabelTree
import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.constants.WorkingRegisters
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager

class FunctionFrameManagerImpl(override val f: FunctionDeclaration, private val analyser: FunctionAnalysisResult) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private var stackOffset: Long = 0

    val label: Label

    init {
        initialiseVariableMap()
        //TODO: Think about it :))
        label = Label(f.name)
    }

    override fun generate_var_access(x: AnyVariable): AssignableTree {
        TODO("Not yet implemented")
    }

    override fun generate_function_call(trees: List<Tree>, result: AssignableTree?, then: CFGNode): CFGNode {
        val outTrees = mutableListOf<Tree>()
        // Put first 2 args to RCX, RDX registers
        if (trees.size >= 1) {
            outTrees.add(
                AssignmentTree(
                    RegisterTree(PhysicalRegister.RCX),
                    trees[0]
                )
            )
        }
        if (trees.size >= 2) {
            outTrees.add(
                AssignmentTree(
                    RegisterTree(PhysicalRegister.RDX),
                    trees[1]
                )
            )
        }
        // Put the rest of the args on stack
        for( i in 2..(trees.size-1) ) {
            outTrees.addAll(
                pushToStack(trees[i])
            )
        }
        // Add Call instruction
        outTrees.add(Call(label))
        
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

    override fun variable_to_virtual_register(x: AnyVariable): UsableMemoryCell {
        if (variableMap[x] == null) {
            throw IllegalArgumentException("Variable is not in scope")
        }

        return variableMap[x]!!
    }

    override fun generate_prolog(then: CFGNode): CFGNode {
        TODO("Not yet implemented")
    }

    override fun generate_epilouge(result: Tree?): CFGNode {
        TODO("Not yet implemented")
    }

    private fun initialiseVariableMap() {
        analyser.variableMap.forEach { (variable, functionDeclaration) ->
            if (functionDeclaration == f) {
                val isNested = analyser.isUsedInNested[variable] ?: false
                val memoryCell: UsableMemoryCell

                if (isNested) {
                    memoryCell = UsableMemoryCell.MemoryPlace(stackOffset * 4)
                    stackOffset += 1
                } else {
                    memoryCell = UsableMemoryCell.VirtReg(VirtualRegister())
                }

                variableMap[variable] = memoryCell
            }
        }
    }

    private fun pushToStack(tree: Tree): List<Tree> {
        return listOf(
            BinaryOperationTree(RegisterTree(PhysicalRegister.RSP), NumericalConstantTree(Register.SIZE), BinaryTreeOperationType.SUBTRACT),
            AssignmentTree(MemoryTree(RegisterTree(PhysicalRegister.RSP)), tree)
        )
    }
}
