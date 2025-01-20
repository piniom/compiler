package org.exeval.instructions.interfaces

import org.exeval.cfg.VirtualRegister
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.instructions.linearizer.BasicBlock

interface SpillsHandler {
	fun handleSpilledVariables(
		blocks: List<BasicBlock>,
		ffm: FunctionFrameManager,
		spills: Set<VirtualRegister>,
	): List<BasicBlock>
}
