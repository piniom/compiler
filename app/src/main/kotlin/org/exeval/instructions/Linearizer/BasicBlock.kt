package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.Label;

data class BasicBlock (
    val label : Label,
    var instructions : List<Instruction>,
    var successors : List<BasicBlock>
)