package org.exeval.instructions

import kotlin.reflect.KClass

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (resultHolder : VirtualRegister?, registers : List<VirtualRegister>) -> List<Instruction>
)

data class InstructionPatternRootType(
    val rootClass: KClass<*>,
    val operationType: Any?
)

sealed class InstructionPattern(
    val rootType: InstructionPatternRootType,
    val kind: InstructionKind,
    val cost: Int
) {
    abstract fun matches(parseTree: Tree): InstructionMatchResult?
}

interface OperandArgumentType

data class NumericalConstant(val value: Long) : OperandArgumentType

class TemplatePattern(
    rootType: InstructionPatternRootType,
    kind: InstructionKind,
    cost: Int,
    val lambdaInstruction: (resultHolder : VirtualRegister?, inputs : List<OperandArgumentType>) -> List<Instruction>
) : InstructionPattern(rootType, kind, cost) {

    // NOTE only simple patterns supported for now
    override fun matches(parseTree: Tree): InstructionMatchResult? {
        if (rootType.rootClass != parseTree::class) {
            return null
        }
        val args = when (parseTree) {
            is BinaryOperationTree -> {
                if (rootType.operationType != parseTree.operation) {
                    return null
                }
                listOf(parseTree.left, parseTree.right)
            }
            is UnaryOperationTree -> {
                if (rootType.operationType != parseTree.operation) {
                    return null
                }
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
        return InstructionMatchResult(toMatch, { dest, registers ->
            lambdaInstruction(dest, injectConstants(constants, registers))
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
