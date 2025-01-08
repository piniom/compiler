package org.exeval.instructions

import kotlin.reflect.KClass

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (resultHolder : Register?, registers : List<VirtualRegister>, label : Label?) -> List<Instruction>
)

interface InstructionPattern{
    val rootType: TreeKind
    val kind: InstructionKind
    val cost: Int
    fun matches(parseTree: Tree): InstructionMatchResult?
}

interface OperandArgumentType

interface AssignableDest : OperandArgumentType

interface ConstantOperandArgumentType : OperandArgumentType

data class NumericalConstant(val value: Long) : ConstantOperandArgumentType
data class DelayedNumericalConstant(val getValue: () -> Long) : ConstantOperandArgumentType

class TemplatePattern(
    override val rootType: TreeKind,
    override val kind: InstructionKind,
    override val cost: Int,
    val lambdaInstruction: (resultHolder : Register?, inputs : List<OperandArgumentType>, label : Label?) -> List<Instruction>
) : InstructionPattern{

    // NOTE only simple patterns supported for now
    override fun matches(parseTree: Tree): InstructionMatchResult? {
        if (rootType != parseTree.treeKind()) {
            return null
        }
        var hasDest = false
        val args = when (parseTree) {
            is StackPushTree -> {
                listOf(parseTree.source)
            }
            is StackPopTree -> {
                listOf(parseTree.destination)
            }
            is BinaryOperationTree -> {
                if (parseTree.left is MemoryTree && parseTree.right is MemoryTree) {
                    return null
                }
                listOf(parseTree.left, parseTree.right)
            }
            is UnaryOperationTree -> {
                listOf(parseTree.child)
            }
            is AssignmentTree -> {
                if (parseTree.destination is MemoryTree && parseTree.value is MemoryTree) {
                    return null
                }
                hasDest = true
                listOf(parseTree.destination, parseTree.value)
            }
            is Call -> {
                listOf(parseTree)
            }
            is Return -> {
                listOf()
            }
            else -> {
                return null
            }
        }

        val toMatch: MutableList<Tree> = mutableListOf()
        val constants: MutableList<OperandArgumentType?> = mutableListOf()
        split(args, constants, toMatch)
        return InstructionMatchResult(toMatch, { dest, registers, label ->
            val combinedArgs = if (dest is VirtualRegister) {
                injectConstants(constants, listOf(dest) + registers).toMutableList()
            }
            else {
                injectConstants(constants, registers).toMutableList()
            }
            if (hasDest) {
                val combinedDest = combinedArgs.removeFirst()
                lambdaInstruction(combinedDest as Register, combinedArgs, label)
            }
            else {
                lambdaInstruction(dest, combinedArgs, label)
            }
        })
    }

    private fun split(args: List<Tree>, constants: MutableList<OperandArgumentType?>, toMatch: MutableList<Tree>) {
        constants.clear()
        constants.addAll(args.map { extractConstant(it) })
        toMatch.clear()
        toMatch.addAll(args.map { if (it is MemoryTree) it.address else it }.filter { extractConstant(it) == null })
    }

    private fun extractConstant(tree: Tree): OperandArgumentType? {
        return when(tree) {
            is LabelConstantTree -> tree.label
            is NumericalConstantTree -> NumericalConstant(tree.value)
            is DelayedNumericalConstantTree -> DelayedNumericalConstant(tree.getValue)
            is Call -> tree.label
            is RegisterTree -> tree.register
            is MemoryTree -> MemoryHolder(extractConstant(tree.address))
            else -> null
        }
    }

    private fun injectConstants(constants: List<OperandArgumentType?>, registers: List<VirtualRegister>): List<OperandArgumentType> {
        val args = constants.toMutableList()
        var i = 0
        for (j in constants.indices) {
            val constant = constants[j]
            if (constant == null) {
                args[j] = registers[i]
                i += 1
            }
            else if (constant is MemoryHolder) {
                if (constant.address == null) {
                    args[j] = Memory(registers[i])
                    i += 1
                }
                else {
                    args[j] = Memory(constant.address)
                }
            }
        }
        return args as List<OperandArgumentType>
    }

    private class MemoryHolder(val address: OperandArgumentType?): OperandArgumentType
}
