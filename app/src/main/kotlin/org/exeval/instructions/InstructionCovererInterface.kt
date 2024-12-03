package org.exeval.instructions;

import org.exeval.cfg.Tree;

interface InstructionCovererInterface {
    fun cover(tree : Tree) : List<Instruction>
}