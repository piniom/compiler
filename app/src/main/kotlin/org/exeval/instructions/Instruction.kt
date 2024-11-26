package org.exeval.instructions

import org.exeval.cfg.OperandArgumentType

enum class OperationAsm {
    MOV, ADD, SUB, MUL, DIV,
    AND, OR, XOR, XCHG, NEG,
    INC, DEC, CALL, RET, CMP,
    JMP, JG, JGE, JE, ADC,
    CMOVG, CMOVGE, CMOVE, JNE
}

data class Instruction(
    val operation: OperationAsm,
    val arguments: List<OperandArgumentType>
)
