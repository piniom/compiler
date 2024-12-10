package org.exeval.instructions.interfaces

import org.exeval.cfg.Register
import org.exeval.instructions.linearizer.BasicBlock
import org.exeval.ffm.interfaces.FunctionFrameManager


interface SpillsHandler {
    fun handleSpilledVariables(instructions: List<BasicBlock>, ffm: FunctionFrameManager, spills: Set<Register>) -> List<BasicBlock>
}


