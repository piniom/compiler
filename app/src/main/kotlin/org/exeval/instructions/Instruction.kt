package org.exeval.instructions

package org.exeval.instructions

import org.exeval.cfg.Register
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.VirtualRegister
import org.exeval.cfg.Label

enum class OperationAsm {
    MOV, ADD, SUB, MUL, DIV,
    AND, OR, XOR, XCHG, NEG,
    INC, DEC, CALL, RET, CMP,
    JMP, JG, JGE, JE, ADC,
    CMOVG, CMOVGE, CMOVE, JNE
}

interface Instruction {
    fun toAsm(mapping: Map<Register, PhysicalRegister>): String
    fun usedRegisters(): List<Register>
    fun definedRegisters(): List<Register>
    fun isCopy(): Boolean
}

// data class SimpleAsmInstruction(
//     val operation: OperationAsm,
//     val arguments: List<OperandArgumentType>
// ) : Instruction

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
        is Label -> {
            arg.name
        }
        else -> {
            throw IllegalArgumentException("Unexpected argument type: $arg")
        }
    }
}

// Instructions definitions
// TODO: Update instructions allowing memory arguments
// MOV instruction
class MovInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>): String =
        "MOV ${argToString(dest, mapping)}, ${argToString(src, mapping)}"

    override fun usedRegisters() = listOfNotNull(src as? Register)

    override fun definedRegisters() = listOfNotNull(dest as? Register)

    override fun isCopy() = src is Register //&& dest is Register 
}

// Arithmetic and Logical instructions

class AddInstruction(val dest: AssignableDest, val src: OperandArgumentType): Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>): String =
        "ADD ${argToString(dest, mapping)}, ${argToString(src, mapping)}"

    override fun usedRegisters() = listOfNotNull(dest as? Register, src as? Register)

    override fun definedRegisters() = listOfNotNull(dest as? Register)

    override fun isCopy() = false
}

class SubInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "SUB ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register, src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = false
}

class MulInstruction(val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "MUL ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(src as? Register)
    override fun definedRegisters() = listOf(PhysicalRegister.RAX, PhysicalRegister.RDX)
    override fun isCopy() = false
}

class DivInstruction(val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "DIV ${argToString(src, mapping)}"
        override fun usedRegisters() = listOf(PhysicalRegister.RAX, PhysicalRegister.RDX)
            + listOfNotNull(src as? Register)
    override fun definedRegisters() = listOf(PhysicalRegister.RAX, PhysicalRegister.RDX)
    override fun isCopy() = false
}

class AndInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "AND ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register, src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = false
}

class OrInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "OR ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register, src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = false
}

class XorInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "XOR ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register, src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
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
    override fun usedRegisters() = listOfNotNull(dest as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = false
}

class IncInstruction(val dest: AssignableDest) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "INC ${argToString(dest, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = false
}

class DecInstruction(val dest: AssignableDest) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "DEC ${argToString(dest, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
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

class CmpInstruction(val left: OperandArgumentType, val right: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "CMP ${argToString(left, mapping)}, ${argToString(right, mapping)}"
    override fun usedRegisters() = listOfNotNull(left as? Register, right as? Register)
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JmpInstruction(val target: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JMP ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JgInstruction(val target: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) =
        "JG ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JgeInstruction(val target: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "JGE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JeInstruction(val target: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "JE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

class JneInstruction(val target: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "JNE ${argToString(target, mapping)}"
    override fun usedRegisters() = emptyList<Register>()
    override fun definedRegisters() = emptyList<Register>()
    override fun isCopy() = false
}

// Conditional move instructions
class AdcInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "ADC ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(dest as? Register, src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = false
}

class CmovgInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "CMOVG ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = src is Register // && dest is Register
}

class CmovgeInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "CMOVGE ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = src is Register // && dest is Register
}

class CmoveInstruction(val dest: AssignableDest, val src: OperandArgumentType) : Instruction {
    override fun toAsm(mapping: Map<Register, PhysicalRegister>) = 
        "CMOVE ${argToString(dest, mapping)}, ${argToString(src, mapping)}"
    override fun usedRegisters() = listOfNotNull(src as? Register)
    override fun definedRegisters() = listOfNotNull(dest as? Register)
    override fun isCopy() = src is Register // && dest is Register
}

