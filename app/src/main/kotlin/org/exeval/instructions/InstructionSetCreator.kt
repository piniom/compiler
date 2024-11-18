package org.exeval.instructions

import org.exeval.cfg.*
import org.exeval.cfg.constants.*

class InstructionSetCreator {
    private val patterns: Map<OperationType, List<InstructionPattern>>

    init {
        patterns = initInstructionSet()
    }

    fun createInstructionSet(): Map<OperationType, List<InstructionPattern>> = patterns


    // Private

    private fun initInstructionSet(): Map<OperationType, List<InstructionPattern>> {
        return mapOf(
            BinaryOperationType.ASSIGNMENT to createAssignmentPatterns(),
            BinaryOperationType.ADD to createSafeSimple2ArgPattern(
                BinaryOperationType.ADD, OperationAsm.ADD),
            BinaryOperationType.SUBTRACT to createSafeSimple2ArgPattern(
                BinaryOperationType.SUBTRACT, OperationAsm.SUB),

            BinaryOperationType.MULTIPLY to createMultiplyPatterns(),
            BinaryOperationType.DIVIDE to createDividePatterns(),
            BinaryOperationType.MODULO to createModuloPatterns(),

            BinaryOperationType.AND to createSimpleBoolOperationPattern(
                BinaryOperationType.AND, OperationAsm.AND),
            BinaryOperationType.OR to createSimpleBoolOperationPattern(
                BinaryOperationType.OR, OperationAsm.OR),
            BinaryOperationType.XOR to createSimpleBoolOperationPattern(
                BinaryOperationType.XOR, OperationAsm.XOR),

            BinaryOperationType.GREATER to createSimpleComparisonPattern(
                BinaryOperationType.GREATER, OperationAsm.CMOVG),
            BinaryOperationType.GREATER_EQUAL to createSimpleComparisonPattern(
                BinaryOperationType.GREATER_EQUAL, OperationAsm.CMOVGE),
            BinaryOperationType.EQUAL to createSimpleComparisonPattern(
                BinaryOperationType.EQUAL, OperationAsm.CMOVE),

            UnaryOperationType.NOT to createNotPatterns(),
            UnaryOperationType.MINUS to createNegationPatterns(),
            UnaryOperationType.INCREMENT to createIncrementPatterns(),
            UnaryOperationType.DECREMENT to createDecrementPatterns(),
            UnaryOperationType.CALL to createCallPatterns(),

            NullaryOperationType.RETURN to createReturnPatterns(),
        )
    }

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.ASSIGNMENT, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister is Memory && operands[0] is Memory) {
                    listOf(
                        Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operands[0])),
                        Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister(WorkingRegisters.R0)))
                    )
                }
                else {
                    listOf(
                        Instruction(OperationAsm.MOV, listOf(destRegister, operands[0]))
                    )
                }
            }
        )
    }

    private fun createMultiplyPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.MULTIPLY, InstructionKind.VALUE, 1) { operands, destRegister ->
                createMulDivModInstructions(OperationAsm.MUL, operands, destRegister)
            }
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.DIVIDE, InstructionKind.VALUE, 1) { operands, destRegister ->
                createMulDivModInstructions(OperationAsm.DIV, operands, destRegister)
            }
        )
    }

    private fun createModuloPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.MODULO, InstructionKind.VALUE, 1) { operands, destRegister ->
                createMulDivModInstructions(OperationAsm.DIV, operands, destRegister) + listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister(WorkingRegisters.R0))),
                )
            }
        )
    }

    private fun createMulDivModInstructions(operation: OperationAsm, operands: List<OperandArgumentType>, destRegister: Assignable): List<Instruction> {
        return listOf(
            Instruction(OperationAsm.MOV, listOf(destRegister, PhysicalRegister(Registers.RAX))),
            Instruction(OperationAsm.MOV, listOf(PhysicalRegister(Registers.RAX), operands[0])),
            Instruction(OperationAsm.MOV, listOf(
                VirtualRegister(WorkingRegisters.R0),
                PhysicalRegister(Registers.RDX)
            ))
        ) + when (operands[1]) {
            // Case: Register or Memory
            is Assignable -> listOf(
                Instruction(operation, listOf(operands[1])),
            )
            // Case: Constant
            is Constant -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R1), operands[1])),
                Instruction(operation, listOf(VirtualRegister(WorkingRegisters.R1))),
            )
        } + listOf(
            Instruction(OperationAsm.XCHG, listOf(destRegister, PhysicalRegister(Registers.RAX))),
            Instruction(OperationAsm.XCHG, listOf(
                PhysicalRegister(Registers.RDX),
                VirtualRegister(WorkingRegisters.R0)
            )),
        )
    }

    private fun createSafeSimple2ArgPattern(rootOperation: BinaryOperationType, asmOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
                TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0]))
                ) + create2ArgInstruction(asmOperation, destRegister, operands[1])
            }
        )
    }

    private fun convertBooleanTo0Or1(destRegister: Register, boolean: OperandArgumentType): List<Instruction> {
        return listOf(
            // A neat conversion without jumps found on stackoverflow

            // Set destRegister to 0
            Instruction(OperationAsm.XOR, listOf(destRegister, destRegister)),
            // Carry will be set if boolean was not 0
            Instruction(OperationAsm.SUB, listOf(destRegister, boolean)),
            // Set destRegister to 0 once again
            Instruction(OperationAsm.XOR, listOf(destRegister, destRegister)),
            // Add carry to destRegister + 0
            Instruction(OperationAsm.ADC, listOf(destRegister, Constant(0))),
            // If carry was set, destRegister will be equal to 1, otherwise it'll be 0
        )
    }

    private fun createSimpleBoolOperationPattern(rootOperation: BinaryOperationType, asmOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
                TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                convertBooleanTo0Or1(VirtualRegister(WorkingRegisters.R1), operands[0]) + listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister(WorkingRegisters.R1)))
                ) + convertBooleanTo0Or1(VirtualRegister(WorkingRegisters.R1), operands[1]) +
                create2ArgInstruction(asmOperation, destRegister, VirtualRegister(WorkingRegisters.R1))
            }
        )
    }

    private fun createSimpleComparisonPattern(rootOperation: BinaryOperationType, asmCmovOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.XOR, listOf(
                        VirtualRegister(WorkingRegisters.R1),
                        VirtualRegister(WorkingRegisters.R1))
                    ),
                    Instruction(OperationAsm.MOV, listOf(destRegister, Constant(1))),
                ) + create2ArgInstruction(OperationAsm.CMP, operands[0], operands[1]) + listOf(
                    // The first operand HAS to be a register (cannot be memory)
                    Instruction(asmCmovOperation, listOf(VirtualRegister(WorkingRegisters.R1), destRegister)),
                    Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister(WorkingRegisters.R1))),
                )
            }
        )
    }

    private fun create2ArgInstruction(operation: OperationAsm, operand1: OperandArgumentType, operand2: OperandArgumentType): List<Instruction> {
        return when {
            // Case: Register + Register
            operand1 is Register && operand2 is Register -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Register + Memory
            operand1 is Register && operand2 is Memory -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Register
            operand1 is Memory && operand2 is Register -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Memory
            operand1 is Memory && operand2 is Memory -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegister(WorkingRegisters.R0))),
            )
            // Case: Register + Constant
            operand1 is Register && operand2 is Constant -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Register
            operand1 is Constant && operand2 is Register -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegister(WorkingRegisters.R0))),
            )
            // Case: Memory + Constant
            operand1 is Memory && operand2 is Constant -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Memory
            operand1 is Constant && operand2 is Memory -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegister(WorkingRegisters.R0))),
            )
            // Case: Constant + Constant
            operand1 is Constant && operand2 is Constant -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegister(WorkingRegisters.R0))),
            )
            else -> throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
        }
    }

    private fun createNotPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.NOT, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannnot be a constant.
                     */
                    Instruction(OperationAsm.SUB, listOf(destRegister, Constant(1))),
                    Instruction(OperationAsm.NEG, listOf(destRegister))
                )
            }
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.MINUS, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    Instruction(OperationAsm.NEG, listOf(destRegister))
                )
            }
        )
    }

    private fun createIncrementPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.INCREMENT, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    Instruction(OperationAsm.INC, listOf(destRegister))
                )
            }
        )
    }

    private fun createDecrementPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.DECREMENT, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    Instruction(OperationAsm.DEC, listOf(destRegister))
                )
            }
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.CALL, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    // The argument must contain the address or label where the target function is located
                    Instruction(OperationAsm.CALL, listOf(operands[0]))
                )
            }
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(NullaryOperationType.RETURN, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.RET, listOf())
                )
            }
        )
    }
}
