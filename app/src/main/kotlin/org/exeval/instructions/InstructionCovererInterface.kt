package org.exeval.instructions

import org.exeval.cfg.Label
import org.exeval.cfg.Tree

interface InstructionCovererInterface {
	fun cover(
		tree: Tree,
		labelTrue: Label?,
	): List<Instruction>
}
