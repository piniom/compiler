package org.exeval.instructions

sealed data class InstructionMatchResult (
    children: List<Tree>,
    createInstruction: (resultHolder : Tree?, registers : List<Register>) -> List<Instruction>
)

sealed class InstructionPattern {
    abstract val kind: InstructionKind
    abstract val rootClass: Tree
    abstract val cost: Int
    fun matches(parseTree: Tree): InstructionMatchResult
}