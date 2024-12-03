package org.exeval.instructions

interface Instruction

enum class OperationAsm {
    MOV, ADD, SUB, MUL, DIV,
    AND, OR, XOR, XCHG, NEG,
    INC, DEC, CALL, RET, CMP,
    JMP, JG, JGE, JE, ADC,
    CMOVG, CMOVGE, CMOVE, JNE
}

data class SimpleAsmInstruction(
    val operation: OperationAsm,
    val arguments: List<OperandArgumentType>
) : Instruction
