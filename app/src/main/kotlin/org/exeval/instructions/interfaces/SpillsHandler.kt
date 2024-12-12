package org.exeval.instructions.interfaces

import org.exeval.cfg.VirtualRegister
import org.exeval.instructions.linearizer.BasicBlock
import org.exeval.ffm.interfaces.FunctionFrameManager


interface SpillsHandler {
    fun handleSpilledVariables(instructions: List<BasicBlock>, ffm: FunctionFrameManager, spills: Set<VirtualRegister>): List<BasicBlock>
}


