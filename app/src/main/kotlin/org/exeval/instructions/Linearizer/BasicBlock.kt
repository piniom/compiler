package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.DataLabel;

data class BasicBlock (
    val label : DataLabel,
    val instructions : List<Instruction>,
    val successors : List<BasicBlock>
)