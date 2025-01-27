package org.exeval.instructions

import org.exeval.cfg.Register
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.VirtualRegister
import org.exeval.cfg.Label
import org.exeval.cfg.Memory

enum class OperationAsm {
    MOV, ADD, SUB, MUL, DIV,
    AND, OR, XOR, XCHG, NEG,
    INC, DEC, CALL, RET, CMP,
    JMP, JG, JGE, JE, ADC,
    CMOVG, CMOVGE, CMOVE, JNE,
    JL, JLE, CMOVL, CMOVLE
}

interface Instruction {
    fun toAsm(mapping: Map<Register, PhysicalRegister>): String
    fun usedRegisters(): List<Register>
    fun definedRegisters(): List<Register>
    fun isCopy(): Boolean
}

fun argToString(arg: OperandArgumentType, mapping: Map<Register, PhysicalRegister>): String {
    return when (arg) {
        is PhysicalRegister -> {
            arg.toString()
        }
        is VirtualRegister -> {
            mapping[arg]!!.toString()
        }
        is NumericalConstant -> {
            arg.value.toString()
        }
        is DelayedNumericalConstant -> {
            arg.getValue().toString()
        }
        is Label -> {
            arg.name
        }
        is Memory -> {
            "[${argToString(arg.address, mapping)}]"
        }
        else -> {
            throw IllegalArgumentException("Unexpected argument type: $arg")
        }
    }
}

fun getRegisters(vararg operands: OperandArgumentType): List<Register> {
    return operands.map { if (it is Memory) it.address else it }.map { it as? Register }.filterNotNull()
}

// Instructions definitions
// MOV instruction
class MovInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>): String =
        "MOV ${argToString(dest, mapping)}, ${argToString(src, mapping)}"

    override fun usedRegisters() = getRegisters(src)

    override fun definedRegisters() = getRegisters(dest)

    override fun isCopy() = src is Register //&& dest is Register
}

// Arithmetic and Logical instructions

class AddInstruction(val dest: AssignableDest, val src: OperandArgumentType): Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>): String =
        "ADD ${argToString(dest, mapping)}, ${argToString(src, mapping)}"

    override fun usedRegisters() = getRegisters(dest, src)

    override fun definedRegisters() = getRegisters(dest)

    override fun isCopy() = false
}

class SubInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "SUB ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(dest, src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class MulInstruction(val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "MUL ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(PhysicalRegister.RAX, src)
    override fun definedRegisters() = listOf(PhysicalRegister.RAX, PhysicalRegister.RDX)
    override fun isCopy() = false
}

class DivInstruction(val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "DIV ${argToString(src, mapping)}"
    override fun usedRegisters() = listOf(
            PhysicalRegister.RAX, PhysicalRegister.RDX
        ) + getRegisters(src)
    override fun definedRegisters() = listOf(PhysicalRegister.RAX, PhysicalRegister.RDX)
    override fun isCopy() = false
}

class AndInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "AND ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(dest, src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class OrInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "OR ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(dest, src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class XorInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "XOR ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(dest, src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class XchgInstruction(val reg1: Register, val reg2: Register) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "XCHG ${argToString(reg1, mapping)}, ${argToString(reg2, mapping)}"
    override fun usedRegisters() = listOf(reg1, reg2)
    override fun definedRegisters() = listOf(reg1, reg2)
    override fun isCopy() = false
}

// Unary instructions
class NegInstruction(val dest: AssignableDest) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "NEG ${argToString(dest, mapping)}"
    override fun usedRegisters() = getRegisters(dest)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class IncInstruction(val dest: AssignableDest) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "INC ${argToString(dest, mapping)}"
    override fun usedRegisters() = getRegisters(dest)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class DecInstruction(val dest: AssignableDest) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "DEC ${argToString(dest, mapping)}"
    override fun usedRegisters() = getRegisters(dest)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

// Control flow instructions
// TODO: Should target be of type Label since we don't use "functional language features"?
class CallInstruction(val target: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CALL ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class RetInstruction : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "RET"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

// NOTE Nothing is assigned to the first argument.
//      However, assembly allows it to be only a register or memory location.
class CmpInstruction(val left: AssignableDest, val right: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMP ${argToString(left, mapping)}, ${argToString(right, mapping)}"
    override fun usedRegisters() = getRegisters(left, right)
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

abstract class JumpInstructionBase(val target: OperandArgumentType) : Instruction

class JmpInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JMP ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JgInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JG ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JgeInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JGE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JeInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JneInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JNE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JlInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JL ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JleInstruction(target: OperandArgumentType) : JumpInstructionBase(target) {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JLE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

// Conditional move instructions
class AdcInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "ADC ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(dest, src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

class CmovgInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMOVG ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = src is Register // && dest is Register
}

class CmovgeInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMOVGE ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = src is Register // && dest is Register
}

class CmoveInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMOVE ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = src is Register // && dest is Register
}

class CmovlInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMOVL ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = src is Register // && dest is Register
}

class CmovleInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMOVLE ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(src)
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = src is Register // && dest is Register
}

// Stack instructions

class PushInstruction(val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "PUSH ${argToString(src, mapping)}"
    override fun usedRegisters() = getRegisters(src)
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class PopInstruction(val dest: AssignableDest) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "POP ${argToString(dest, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = getRegisters(dest)
    override fun isCopy() = false
}

