package org.exeval.instructions

import kotlin.reflect.KClass

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (resultHolder : VirtualRegister?, registers : List<VirtualRegister>, label : Label?) -> List<Instruction>
)

interface InstructionPattern{
    val rootType: TreeKind
    val kind: InstructionKind
    val cost: Int
    fun matches(parseTree: Tree): InstructionMatchResult?
}

interface OperandArgumentType

interface ConstantOperandArgumentType : OperandArgumentType

data class NumericalConstant(val value: Long) : ConstantOperandArgumentType

class TemplatePattern(
    override val rootType: TreeKind,
    override val kind: InstructionKind,
    override val cost: Int,
    val lambdaInstruction: (resultHolder : VirtualRegister?, inputs : List<OperandArgumentType>, label : Label?) -> List<Instruction>
) : InstructionPattern{

    // NOTE only simple patterns supported for now
    override fun matches(parseTree: Tree): InstructionMatchResult? {
        if (rootType != parseTree.treeKind()) {
            return null
        }
        val args = when (parseTree) {
            is BinaryOperationTree -> {
                listOf(parseTree.left, parseTree.right)
            }
            is UnaryOperationTree -> {
                listOf(parseTree.child)
            }
            is AssignmentTree -> {
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
            lambdaInstruction(dest, injectConstants(constants, registers), label)
        })
    }

    private fun split(args: List<Tree>, constants: MutableList<OperandArgumentType?>, toMatch: MutableList<Tree>) {
        constants.clear()
        constants.addAll(args.map { extractConstant(it) })
        toMatch.clear()
        toMatch.addAll(args.filter { !(it is ConstantTree) })
    }

    private fun extractConstant(tree: Tree): OperandArgumentType? {
        return when(tree) {
            is LabelConstantTree -> tree.label
            is NumericalConstantTree -> NumericalConstant(tree.value)
            is Call -> tree.label
            else -> null
        }
    }

    private fun injectConstants(constants: List<OperandArgumentType?>, registers: List<VirtualRegister>): List<OperandArgumentType> {
        val args = constants.toMutableList()
        var i = 0
        for (j in constants.indices) {
            if (constants[j] == null) {
                args[j] = registers[i]
                i += 1
            }
        }
        return args as List<OperandArgumentType>
    }
}
