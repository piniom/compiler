package org.exeval.instructions.linearizer

import org.exeval.cfg.Label
import org.exeval.instructions.Instruction

data class BasicBlock(
	val label: Label,
	var instructions: List<Instruction>,
	var successors: List<BasicBlock>,
)
