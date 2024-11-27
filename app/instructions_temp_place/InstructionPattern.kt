package org.exeval.instructions

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (operands : List<OperandArgumentTypeTree>, destRegister : AssignableTree?) -> List<Instruction>
)

sealed class InstructionPattern(
    val rootClass: TreeOperationType,
    val kind: InstructionKind,
    val cost: Int
) {
    abstract fun matches(parseTree: Tree): InstructionMatchResult?
}

class TemplatePattern(
    rootClass: TreeOperationType,
    kind: InstructionKind,
    cost: Int,
    val lambdaInstruction: (operands : List<OperandArgumentTypeTree>, destRegister : AssignableTree?) -> List<Instruction>
) : InstructionPattern(rootClass, kind, cost) {

    override fun matches(parseTree: Tree): InstructionMatchResult? {
        return when (parseTree) {
            is Call, is Return -> {
                if (rootClass is NullaryTreeOperationType) {
                    InstructionMatchResult(emptyList(), lambdaInstruction)
                }
                else null
            }

            is AssignmentTree -> {
                if (rootClass == BinaryTreeOperationType.ASSIGNMENT) {
                    InstructionMatchResult(listOf(parseTree.value), lambdaInstruction)
                }
                else null
            }

            is UnaryOperationTree -> {
                if (parseTree.operation == rootClass) {
                    InstructionMatchResult(listOf(parseTree.child), lambdaInstruction)
                }
                else null
            }

            is BinaryOperationTree -> {
                if (parseTree.operation == rootClass) {
                    InstructionMatchResult(listOf(parseTree.left, parseTree.right), lambdaInstruction)
                }
                else null
            }

            else -> null
        }
    }
}
