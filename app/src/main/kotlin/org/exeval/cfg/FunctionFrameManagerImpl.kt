package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalyser
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.CFGNodeImpl
import org.exeval.cfg.constants.Registers
import org.exeval.cfg.constants.WorkingRegisters
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager

class FunctionFrameManagerImpl(override val f: FunctionDeclaration, private val analyser: FunctionAnalysisResult) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private var virtualRegIdx = WorkingRegisters.REGISTER_SIZE
    private var stackOffset = 0

    init {
        initialiseVariableMap()
    }

    override fun generate_var_access(x: AnyVariable): Tree {
        TODO("Not yet implemented")
    }

    override fun generate_function_call(trees: List<Tree>, result: Assignable?, then: CFGNode): CFGNode {
        val outTrees = mutableListOf<Tree>()
        // Put first 2 args to RCX, RDX registers
        if (trees.size >= 1) {
            outTrees.add(
                Assigment(
                    PhysicalRegister(Registers.RCX),
                    trees[0]
                )
            )
        }
        if (trees.size >= 2) {
            outTrees.add(
                Assigment(
                    PhysicalRegister(Registers.RDX),
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
        outTrees.add(Call)
        
        // Store result from RAX if needed
        result?.let {
            outTrees.add(
                Assigment(
                    it,
                    PhysicalRegister(Registers.RAX)
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
                    memoryCell = UsableMemoryCell.VirtReg(virtualRegIdx)
                    virtualRegIdx += 1
                }

                variableMap[variable] = memoryCell
            }
        }
    }

    private fun pushToStack(tree: Tree): List<Tree> {
        return listOf(
            BinaryOperation(PhysicalRegister(Registers.RSP), Constant(Registers.REGISTER_SIZE), BinaryOperationType.SUBTRACT),
            Assigment(Memory(PhysicalRegister(Registers.RSP)), tree)
        )
    }
}