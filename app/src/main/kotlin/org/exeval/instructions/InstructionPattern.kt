package org.exeval.instructions

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (operands : List<OperandArgumentType>, destRegister : Assignable) -> List<Instruction>
)

sealed class InstructionPattern(
    val rootClass: OperationType,
    val kind: InstructionKind,
    val cost: Int
) {
    abstract fun matches(parseTree: Tree): InstructionMatchResult?
}

class TemplatePattern(
    rootClass: OperationType,
    kind: InstructionKind,
    cost: Int,
    val lambdaInstruction: (operands : List<OperandArgumentType>, destRegister : Assignable) -> List<Instruction>
) : InstructionPattern(rootClass, kind, cost) {

    override fun matches(parseTree: Tree): InstructionMatchResult? {
        return when (parseTree) {
            is Call, is Return -> {
                if (rootClass is NullaryOperationType) {
                    InstructionMatchResult(emptyList(), lambdaInstruction)
                }
                else null
            }

            is Assigment -> {
                if (rootClass == BinaryOperationType.ASSIGNMENT) {
                    InstructionMatchResult(listOf(parseTree.value), lambdaInstruction)
                }
                else null
            }

            is UnaryOp -> {
                if (parseTree.operation == rootClass) {
                    InstructionMatchResult(listOf(parseTree.child), lambdaInstruction)
                }
                else null
            }

            is BinaryOperation -> {
                if (parseTree.operation == rootClass) {
                    InstructionMatchResult(listOf(parseTree.left, parseTree.right), lambdaInstruction)
                }
                else null
            }

            else -> null
        }
    }
}
