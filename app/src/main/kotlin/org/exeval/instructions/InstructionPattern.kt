package org.exeval.instructions

import org.exeval.cfg.Tree
import org.exeval.cfg.Register


data class InstructionMatchResult (
    val children: List<Tree>,
    val createInstruction: (resultHolder : Tree?, registers : List<Register>) -> List<Instruction>
)

sealed class InstructionPattern {
    abstract val kind: InstructionKind
    abstract val rootClass: Tree
    abstract val cost: Int
    abstract fun matches(parseTree: Tree): InstructionMatchResult
}