package org.exeval.instructions

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (resultHolder : Tree?, registers : List<RegisterTree>) -> List<Instruction>
)

sealed class InstructionPattern(
    val rootClass: Any, //TreeOperationType,
    val kind: InstructionKind,
    val cost: Int
) {
    abstract fun matches(parseTree: Tree): InstructionMatchResult?
}

class TemplatePattern(
    rootClass: Any, //TreeOperationType,
    kind: InstructionKind,
    cost: Int,
    val lambdaInstruction: (resultHolder : Tree?, registers : List<RegisterTree>) -> List<Instruction>
) : InstructionPattern(rootClass, kind, cost) {

    override fun matches(parseTree: Tree): InstructionMatchResult? {
        return when (parseTree) {
            is Call, is Return -> {
                if (rootClass == Call::class || rootClass == Return::class) {
                    InstructionMatchResult(emptyList(), lambdaInstruction)
                }
                else null
            }

            is AssignmentTree -> {
                if (rootClass == AssignmentTree::class) {
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
