package org.exeval.instructions

import kotlin.reflect.KClass

import org.exeval.cfg.*


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (resultHolder : Tree?, registers : List<RegisterTree>) -> List<Instruction>
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

class TemplatePattern(
    rootType: InstructionPatternRootType,
    kind: InstructionKind,
    cost: Int,
    val lambdaInstruction: (resultHolder : Tree?, registers : List<RegisterTree>) -> List<Instruction>
) : InstructionPattern(rootType, kind, cost) {

    override fun matches(parseTree: Tree): InstructionMatchResult? {
        return null // TODO fix
        /*
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
        */
    }
}
