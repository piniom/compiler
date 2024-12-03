package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.Label;

data class BasicBlock (
    val label : Label,
    val instructions : List<Instruction>,
    val successors : List<BasicBlock>
)