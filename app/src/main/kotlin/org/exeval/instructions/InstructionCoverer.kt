package org.exeval.instructions

import org.exeval.cfg.Tree

class InstructionCoverer(private val instructionPatterns : Set<InstructionPattern>) {
    
    public fun cover(tree : Tree) : List<Instruction> {
        return mutableListOf()
    }
}