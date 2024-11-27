package org.exeval.instructions

typealias OperandArgumentTypeTree = Any

sealed interface Instruction

enum class OperationAsm {
    MOV, ADD, SUB, MUL, DIV,
    AND, OR, XOR, XCHG, NEG,
    INC, DEC, CALL, RET, CMP,
    JMP, JG, JGE, JE, ADC,
    CMOVG, CMOVGE, CMOVE
}

data class SimpleAsmInstruction(
    val operation: OperationAsm,
    val arguments: List<OperandArgumentTypeTree>
) : Instruction
