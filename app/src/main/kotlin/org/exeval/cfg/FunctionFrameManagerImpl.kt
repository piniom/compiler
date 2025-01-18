package org.exeval.cfg

import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.utilities.getMangledFunctionName

private const val BYTES_IN_WORD = 8

class FunctionFrameManagerImpl(
    override val f: FunctionDeclaration,
    private val analyser: FunctionAnalysisResult,
    private val otherManagers: Map<FunctionDeclaration, FunctionFrameManager>
) : FunctionFrameManager {
    private val variableMap = mutableMapOf<AnyVariable, UsableMemoryCell>()
    private var stackOffset = LockableBox<Long>(0)
    private var displayBackupVirtualRegister: VirtualRegister = VirtualRegister()

    val label: Label
    private val calleSaveRegisters = listOf(
        PhysicalRegister.R9,
        PhysicalRegister.R10,
        PhysicalRegister.R11,
        PhysicalRegister.R12,
        PhysicalRegister.R13,
        PhysicalRegister.R14,
        PhysicalRegister.R15,
    )

    init {
        initialiseVariableMap()
        label = Label(getMangledFunctionName(f.name))
    }

    override fun generate_var_access(x: AnyVariable, functionFrameOffset: Tree): AssignableTree {
        if (variableMap[x] == null) {
            val variableParent = analyser.variableMap[x] ?: throw IllegalArgumentException("Unknown Variable")
            val frameManager = otherManagers[variableParent] ?: throw IllegalArgumentException("Unknown Function")
            val nestedLevel = getNestingLevel(variableParent)
            val parentOffset = getDisplayMemory(nestedLevel)
            return frameManager.generate_var_access(x, parentOffset)
        } else {
            return VarAccessGenerator(functionFrameOffset).generateVarAccess(variableMap[x]!!)
        }
    }

    override fun generate_function_call(trees: List<Tree>, result: AssignableTree?, then: CFGNode): CFGNode {
        val outTrees = mutableListOf<Tree>()
        // Put first 2 args to RCX, RDX registers
        if (trees.isNotEmpty()) {
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
        for (i in 2..<trees.size) {
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
        val trees = mutableListOf<Tree>()

        trees.add(AssignmentTree(RegisterTree(PhysicalRegister.RBP), RegisterTree(PhysicalRegister.RSP)))
        trees.addAll(updateDisplay())

        if (f.parameters.isNotEmpty()) {
            trees.add(AssignmentTree(generate_var_access(f.parameters[0]), RegisterTree(PhysicalRegister.RCX)))
        }

        if (f.parameters.size >= 2) {
            trees.add(AssignmentTree(generate_var_access(f.parameters[1]), RegisterTree(PhysicalRegister.RDX)))
        }

        for (i in 2 until f.parameters.size) {
            val fromStack = MemoryTree(
                BinaryOperationTree(
                    RegisterTree(PhysicalRegister.RSP),
                    NumericalConstantTree(((i - 2) * BYTES_IN_WORD).toLong()),
                    BinaryTreeOperationType.ADD
                )
            )

            trees.add(AssignmentTree(generate_var_access(f.parameters[i]), fromStack))
        }

        trees.addAll(backupRegisters())

        return CFGNodeImpl(
            Pair(then, null),
            trees
        )
    }

    override fun generate_epilouge(result: Tree?): CFGNode {
        val trees = mutableListOf<Tree>()

        // TODO fix, causes compiled program to segfault
        /*
        trees.add(
            AssignmentTree(
                RegisterTree(PhysicalRegister.RSP),
                BinaryOperationTree(
                    RegisterTree(PhysicalRegister.RSP),
                    DelayedNumericalConstantTree { stackOffset.lock(); stackOffset.value * BYTES_IN_WORD },
                    BinaryTreeOperationType.ADD
                )
            )
        )
        */
        trees.addAll(restoreDisplay())
        trees.addAll(restoreRegisters())

        result?.let {
            trees.add(AssignmentTree(RegisterTree(PhysicalRegister.RAX), it))
        }

        trees.add(Return)

        return CFGNodeImpl(null, trees)
    }

    override fun alloc_frame_memory(): AssignableTree {
        val curOffset = stackOffset.value * BYTES_IN_WORD
        stackOffset.value += 1

        val resTree =  MemoryTree(
            BinaryOperationTree(
                RegisterTree(PhysicalRegister.RBP),
                NumericalConstantTree(curOffset),
                BinaryTreeOperationType.ADD
            )
        )
        return resTree
    }

    private fun backupRegisters(): List<Tree> {
        stackOffset.value += calleSaveRegisters.size

        return calleSaveRegisters.map {
            pushToStack(RegisterTree(it))
        }.flatten()
    }

    private fun restoreRegisters(): List<Tree> {
        return calleSaveRegisters.map { popFromStack(RegisterTree(it)) }.flatten()
    }

    private fun initialiseVariableMap() {
        analyser.variableMap.forEach { (variable, functionDeclaration) ->
            if (functionDeclaration == f) {
                val isNested = analyser.isUsedInNested[variable] ?: false
                val memoryCell: UsableMemoryCell

                if (isNested) {
                    memoryCell = UsableMemoryCell.MemoryPlace(stackOffset.value * BYTES_IN_WORD)
                    stackOffset.value += 1
                } else {
                    memoryCell = UsableMemoryCell.VirtReg(VirtualRegister())
                }

                variableMap[variable] = memoryCell
            }
        }
    }

    private fun updateDisplay(): List<Tree> {
        val trees = mutableListOf<Tree>()

        val nestingLevel = getNestingLevel()
        displayBackupVirtualRegister = VirtualRegister()

        trees.add(
            AssignmentTree(
                RegisterTree(displayBackupVirtualRegister),
                getDisplayMemory(nestingLevel)
            )
        )

        trees.add(
            AssignmentTree(
                getDisplayMemory(nestingLevel),
                RegisterTree(PhysicalRegister.RBP)
            )
        )

        return trees
    }

    private fun restoreDisplay(): List<Tree> {
        return mutableListOf<Tree>(
            AssignmentTree(
                getDisplayMemory(getNestingLevel()),
                RegisterTree(displayBackupVirtualRegister)
            )
        )
    }

    private fun pushToStack(tree: Tree): List<Tree> {
        return listOf(StackPushTree(tree))
    }

    private fun popFromStack(toAssign: AssignableTree): List<Tree> {
        return listOf(StackPopTree(toAssign))
    }

    private fun getDisplayMemory(idx: Long): MemoryTree {
        return MemoryTree(
            BinaryOperationTree(
                LabelConstantTree(Label.DISPLAY),
                NumericalConstantTree(idx * BYTES_IN_WORD),
                BinaryTreeOperationType.ADD
            )
        )
    }

    private fun getNestingLevel(fnDecl: FunctionDeclaration = f): Long {
        var result: Long = 0

        var decl: FunctionDeclaration? = fnDecl
        while (analyser.staticParents[decl] != null) {
            result += 1
            decl = analyser.staticParents[decl]
        }

        return result
    }
}

class LockableBox<T>(value: T) {
    var value: T = value
        set(newValue) {
            if (isLocked)
                throw RuntimeException("This box is locked, so you cannot change value")
            field = newValue
        }

    fun lock() {
        isLocked = true
    }

    private var isLocked = false
}