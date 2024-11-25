package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.constants.DISPLAY_LABEL
import org.exeval.cfg.constants.Registers
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager


class FunctionFrameManagerImpl(override val f: FunctionDeclaration, private val analyser: FunctionAnalysisResult, private val otherManagers: Map<FunctionDeclaration, FunctionFrameManager>) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private var virtualRegIdx = 0
    private var stackOffset = 0
    private var displayBackupIdx: Int = 0
    private val calleSaveRegisters = listOf(
        PhysicalRegister(9),
        PhysicalRegister(10),
        PhysicalRegister(11),
        PhysicalRegister(12),
        PhysicalRegister(13),
        PhysicalRegister(14),
        PhysicalRegister(15)
    )

    init {
        initialiseVariableMap()
    }

    override fun generate_var_access(x: AnyVariable, functionFrameOffset: Tree): Assignable {
        if ( variableMap[x] == null ) {
            val variableParent = analyser.variableMap[x] ?: throw IllegalArgumentException("Unknown Variable")
            val frameManager: FunctionFrameManager = otherManagers[variableParent]!!
            val nestedLevel = getNestingLevel(variableParent)
            val parentOffset = getDisplayMemory(nestedLevel)
            return frameManager.generate_var_access(x, parentOffset)
        } else {
            return VarAccessGenerator(functionFrameOffset).generateVarAccess(variableMap[x]!!)
        }
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
        val trees = mutableListOf<Tree>()

        trees.add(Assigment(PhysicalRegister(Registers.RBP), PhysicalRegister(Registers.RSP)))
        trees.addAll(updateDisplay())

        if (f.parameters.isNotEmpty()) {
            trees.add(Assigment(generate_var_access(f.parameters[0]), PhysicalRegister(Registers.RCX)))
        }

        if (f.parameters.size >= 2) {
            trees.add(Assigment(generate_var_access(f.parameters[1]), PhysicalRegister(Registers.RDX)))
        }

        for (i in 2 until f.parameters.size) {
            val fromStack = Memory(
                BinaryOperation(
                    PhysicalRegister(Registers.RSP),
                    Constant((i - 2) * 4),
                    BinaryOperationType.ADD
                )
            )

            trees.add(Assigment(generate_var_access(f.parameters[i]), fromStack))
        }

        trees.addAll(backupRegisters())

        return CFGNodeImpl(
            Pair(then, null),
            trees
        )
    }

    override fun generate_epilouge(result: Tree?): CFGNode {
        val trees = mutableListOf<Tree>()

        trees.add(BinaryOperation(PhysicalRegister(Registers.RSP), Constant(stackOffset * 4), BinaryOperationType.ADD))
        trees.addAll(restoreDisplay())
        trees.addAll(restoreRegisters())

        result?.let {
            trees.add(Assigment(PhysicalRegister(Registers.RAX), it))
        }

        trees.add(Return)

        return CFGNodeImpl(null, trees)
    }

    private fun backupRegisters(): List<Tree> {
        stackOffset += calleSaveRegisters.size

        return calleSaveRegisters.map {
            Assigment(
                Memory(
                    BinaryOperation(
                        PhysicalRegister(Registers.RSP),
                        Constant(calleSaveRegisters.indexOf(it) * Registers.REGISTER_SIZE),
                        BinaryOperationType.ADD
                    )
                ),
                it
            )
        }
    }


    private fun restoreRegisters(): List<Tree> {
        return calleSaveRegisters.map { popFromStack(it) }.flatten()
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
                    memoryCell = UsableMemoryCell.VirtReg(getNextVirtualRegisterIdx())
                }

                variableMap[variable] = memoryCell
            }
        }
    }

    private fun getNextVirtualRegisterIdx(): Int {
        val out = virtualRegIdx
        virtualRegIdx += 1
        return out
    }

    private fun updateDisplay(): List<Tree> {
        val trees = mutableListOf<Tree>()

        val nestingLevel = getNestingLevel()
        displayBackupIdx = getNextVirtualRegisterIdx()

        trees.add(
            Assigment(
                VirtualRegister(displayBackupIdx),
                getDisplayMemory(nestingLevel)
            )
        )

        trees.add(
            Assigment(
                getDisplayMemory(nestingLevel),
                PhysicalRegister(Registers.RBP)
            )
        )

        return trees
    }

    private fun restoreDisplay(): List<Tree> {
        return  mutableListOf<Tree>(
            Assigment(
                getDisplayMemory(getNestingLevel()),
                VirtualRegister(displayBackupIdx)
            )
        )
    }

    private fun pushToStack(tree: Tree): List<Tree> {
        return listOf(
            BinaryOperation(PhysicalRegister(Registers.RSP), Constant(Registers.REGISTER_SIZE), BinaryOperationType.SUBTRACT),
            Assigment(Memory(PhysicalRegister(Registers.RSP)), tree)
        )
    }

    private fun popFromStack(toAssign: Assignable): List<Tree> {
        return listOf(
            Assigment(toAssign, Memory(PhysicalRegister(Registers.RSP))),
            Assigment(PhysicalRegister(Registers.RSP), BinaryOperation(PhysicalRegister(Registers.RSP), Constant(Registers.REGISTER_SIZE), BinaryOperationType.ADD))
        )
    }

    private fun getDisplayMemory(idx: Int): Memory {
        return Memory(
            BinaryOperation(
                DataLabel(DISPLAY_LABEL),
                Constant(idx * 8),
                BinaryOperationType.ADD
            )
        )
    }

    private fun getNestingLevel(fnDecl: FunctionDeclaration = f): Int {
        var result = 0

        var decl: FunctionDeclaration? = fnDecl
        while(analyser.staticParents[decl] != null) {
            result += 1
            decl = analyser.staticParents[decl]
        }

        return result
    }
}