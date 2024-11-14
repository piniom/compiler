package org.exeval.instructions

import org.exeval.cfg.OperandArgumentType

enum class OperationAsm {
    MOV, ADD, SUB, MUL, DIV
}

data class Instruction(
    val operation: OperationAsm,
    val arguments: List<OperandArgumentType>
)