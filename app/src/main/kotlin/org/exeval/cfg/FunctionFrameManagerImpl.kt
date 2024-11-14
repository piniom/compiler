package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalyser
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.ffm.interfaces.FunctionCallResult

class FunctionFrameManagerImpl(override val f: FunctionDeclaration, private val analyser: FunctionAnalysisResult) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private var virtualRegIdx = 0
    private var stackOffset = 0

    init {
        initialiseVariableMap()
    }

    override fun generate_var_access(x: AnyVariable): Tree {
        TODO("Not yet implemented")
    }

    override fun generate_function_call(trees: List<Tree>, then: CFGNode): FunctionCallResult {
        TODO("Not yet implemented")
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
}