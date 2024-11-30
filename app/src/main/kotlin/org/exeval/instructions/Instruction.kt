package org.exeval.instructions

/* TODO fix usages; it's not and will not be a real type
 *      Ideally would be a one-of of register, label, or constant
 *      (or possibly an arithmetic expression using these).
 */
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
