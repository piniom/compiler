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

            BinaryOperationType.AND to createAndPatterns(),
            BinaryOperationType.OR to createOrPatterns(),
            BinaryOperationType.XOR to createSimpleBoolOperationPattern(
                BinaryOperationType.XOR, OperationAsm.XOR),

            BinaryOperationType.GREATER to createSimpleComparisonPattern(
                BinaryOperationType.GREATER, OperationAsm.CMOVG, OperationAsm.JG),
            BinaryOperationType.GREATER_EQUAL to createSimpleComparisonPattern(
                BinaryOperationType.GREATER_EQUAL, OperationAsm.CMOVGE, OperationAsm.JGE),
            BinaryOperationType.EQUAL to createSimpleComparisonPattern(
                BinaryOperationType.EQUAL, OperationAsm.CMOVE, OperationAsm.JE),

            UnaryOperationType.NOT to createNotPatterns(),
            UnaryOperationType.MINUS to createNegationPatterns(),
            UnaryOperationType.INCREMENT to createIncrementPatterns(),
            UnaryOperationType.DECREMENT to createDecrementPatterns(),
            UnaryOperationType.CALL to createCallPatterns(),
            UnaryOperationType.IF to createIfPatterns(),

            NullaryOperationType.RETURN to createReturnPatterns(),
        )
    }

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.ASSIGNMENT, InstructionKind.EXEC, 1) { operands, _ ->
                if (!(operands[0] is Assignable)) {
                    throw IllegalArgumentException("First operand for assignment must be an assignable")
                }
                if (operands[1] is Label) {
                    throw IllegalArgumentException("Cannot assign from label")
                }
                if (operands[0] is Memory && operands[1] is Memory) {
                    listOf(
                        Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operands[1])),
                        Instruction(OperationAsm.MOV, listOf(operands[0], VirtualRegister(WorkingRegisters.R0)))
                    )
                }
                else {
                    listOf(
                        Instruction(OperationAsm.MOV, listOf(operands[0], operands[1]))
                    )
                }
            }
        )
    }

    private fun createMultiplyPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.MULTIPLY, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "multiply")
                verifyNotLabels(operands, "multiply")
                createMulDivModInstructions(OperationAsm.MUL, operands, dest[0] as Assignable)
            },
            createEmptyExecPattern(BinaryOperationType.MULTIPLY)
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.DIVIDE, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "divide")
                verifyNotLabels(operands, "divide")
                createMulDivModInstructions(OperationAsm.DIV, operands, dest[0] as Assignable)
            },
            createEmptyExecPattern(BinaryOperationType.DIVIDE)
        )
    }

    private fun createModuloPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.MODULO, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "modulo")
                verifyNotLabels(operands, "modulo")
                createMulDivModInstructions(OperationAsm.DIV, operands, dest[0] as Assignable) + listOf(
                    Instruction(OperationAsm.MOV, listOf(dest[0], VirtualRegister(WorkingRegisters.R0))),
                )
            },
            createEmptyExecPattern(BinaryOperationType.MODULO)
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
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "2-argument operation ${rootOperation}")
                verifyNotLabels(operands, "2-argument operation ${rootOperation}")
                listOf(
                    Instruction(OperationAsm.MOV, listOf(dest[0], operands[0]))
                ) + create2ArgInstruction(asmOperation, dest[0], operands[1])
            },
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun createAndPatterns(): List<InstructionPattern> {
        return createSimpleBoolOperationPattern(BinaryOperationType.AND, OperationAsm.AND) + listOf(
            TemplatePattern(BinaryOperationType.AND, InstructionKind.JUMP, 1) { operands, dest ->
                verifyLabels(dest, "boolean and")
                verifyNotLabels(operands, "boolean and")
                listOf(
                    Instruction(OperationAsm.CMP, listOf(operands[0], Constant(0))),
                    Instruction(OperationAsm.JE, listOf(dest[1])),
                    Instruction(OperationAsm.CMP, listOf(operands[1], Constant(0))),
                    Instruction(OperationAsm.JE, listOf(dest[1])),
                    Instruction(OperationAsm.JMP, listOf(dest[0])),
                )
            }
        )
    }

    private fun createOrPatterns(): List<InstructionPattern> {
        return createSimpleBoolOperationPattern(BinaryOperationType.OR, OperationAsm.OR) + listOf(
            TemplatePattern(BinaryOperationType.OR, InstructionKind.JUMP, 1) { operands, dest ->
                verifyLabels(dest, "boolean or")
                verifyNotLabels(operands, "boolean or")
                listOf(
                    Instruction(OperationAsm.CMP, listOf(operands[0], Constant(0))),
                    Instruction(OperationAsm.JNE, listOf(dest[0])),
                    Instruction(OperationAsm.CMP, listOf(operands[1], Constant(0))),
                    Instruction(OperationAsm.JNE, listOf(dest[0])),
                    Instruction(OperationAsm.JMP, listOf(dest[1])),
                )
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
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "2-argument boolean operation ${rootOperation}")
                verifyNotLabels(operands, "2-argument boolean operation ${rootOperation}")
                convertBooleanTo0Or1(VirtualRegister(WorkingRegisters.R1), operands[0]) + listOf(
                    Instruction(OperationAsm.MOV, listOf(dest[0], VirtualRegister(WorkingRegisters.R1)))
                ) + convertBooleanTo0Or1(VirtualRegister(WorkingRegisters.R1), operands[1]) +
                create2ArgInstruction(asmOperation, dest[0], VirtualRegister(WorkingRegisters.R1))
            },
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun createSimpleComparisonPattern(rootOperation: BinaryOperationType, asmCmovOperation: OperationAsm, asmJccOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "comparison")
                verifyNotLabels(operands, "comparison")
                listOf(
                    Instruction(OperationAsm.XOR, listOf(
                        VirtualRegister(WorkingRegisters.R1),
                        VirtualRegister(WorkingRegisters.R1))
                    ),
                    Instruction(OperationAsm.MOV, listOf(dest[0], Constant(1))),
                ) + create2ArgInstruction(OperationAsm.CMP, operands[0], operands[1]) + listOf(
                    // The first operand HAS to be a register (cannot be memory)
                    Instruction(asmCmovOperation, listOf(VirtualRegister(WorkingRegisters.R1), dest[0])),
                    Instruction(OperationAsm.MOV, listOf(dest[0], VirtualRegister(WorkingRegisters.R1))),
                )
            },
            TemplatePattern(rootOperation, InstructionKind.JUMP, 1) { operands, dest ->
                verifyLabels(dest, "comparison")
                verifyNotLabels(operands, "comparison")
                create2ArgInstruction(OperationAsm.CMP, operands[0], operands[1]) + listOf(
                    Instruction(asmJccOperation, listOf(dest[0])),
                    Instruction(OperationAsm.JMP, listOf(dest[1]))
                )
            },
            createEmptyExecPattern(rootOperation)
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
            TemplatePattern(UnaryOperationType.NOT, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "boolean negation")
                verifyNotLabels(operands, "boolean negation")
                listOf(
                    Instruction(OperationAsm.MOV, listOf(dest[0], operands[0])),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannnot be a constant.
                     */
                    Instruction(OperationAsm.SUB, listOf(dest[0], Constant(1))),
                    Instruction(OperationAsm.NEG, listOf(dest[0]))
                )
            },
            TemplatePattern(UnaryOperationType.NOT, InstructionKind.JUMP, 1) { operands, dest ->
                verifyLabels(dest, "boolean negation")
                verifyNotLabels(operands, "boolean negation")
                listOf(
                    Instruction(OperationAsm.CMP, listOf(operands[0], Constant(0))),
                    Instruction(OperationAsm.JE, listOf(dest[0])),
                    Instruction(OperationAsm.JMP, listOf(dest[1])),
                )
            },
            createEmptyExecPattern(UnaryOperationType.NOT)
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.MINUS, InstructionKind.VALUE, 1) { operands, dest ->
                verifyAssignable(dest, "negation")
                verifyNotLabels(operands, "negation")
                listOf(
                    Instruction(OperationAsm.MOV, listOf(dest[0], operands[0])),
                    Instruction(OperationAsm.NEG, listOf(dest[0]))
                )
            },
            createEmptyExecPattern(UnaryOperationType.MINUS)
        )
    }

    private fun createIncrementPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.INCREMENT, InstructionKind.VALUE, 1) { operands, dest ->
                if (!(operands[0] is Assignable)) {
                    throw IllegalArgumentException("First operand for increment must be an assignable")
                }
                verifyAssignable(dest, "increment")
                listOf(
                    Instruction(OperationAsm.INC, listOf(operands[0])),
                    Instruction(OperationAsm.MOV, listOf(dest[0], operands[0])),
                )
            },
            TemplatePattern(UnaryOperationType.INCREMENT, InstructionKind.EXEC, 1) { operands, _ ->
                if (!(operands[0] is Assignable)) {
                    throw IllegalArgumentException("First operand for increment must be an assignable")
                }
                listOf(
                    Instruction(OperationAsm.INC, listOf(operands[0])),
                )
            }
        )
    }

    private fun createDecrementPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.DECREMENT, InstructionKind.VALUE, 1) { operands, dest ->
                if (!(operands[0] is Assignable)) {
                    throw IllegalArgumentException("First operand for decrement must be an assignable")
                }
                verifyAssignable(dest, "decrement")
                listOf(
                    Instruction(OperationAsm.DEC, listOf(operands[0])),
                    Instruction(OperationAsm.MOV, listOf(dest[0], operands[0])),
                )
            },
            TemplatePattern(UnaryOperationType.DECREMENT, InstructionKind.EXEC, 1) { operands, _ ->
                if (!(operands[0] is Assignable)) {
                    throw IllegalArgumentException("First operand for decrement must be an assignable")
                }
                listOf(
                    Instruction(OperationAsm.DEC, listOf(operands[0])),
                )
            }
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.CALL, InstructionKind.EXEC, 1) { operands, _ ->
                listOf(
                    // The argument must contain the address or label where the target function is located
                    Instruction(OperationAsm.CALL, listOf(operands[0]))
                )
            }
        )
    }

    private fun createIfPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryOperationType.CALL, InstructionKind.JUMP, 1) { operands, dest ->
                verifyLabels(dest, "if")
                verifyNotLabels(operands, "if")
                listOf(
                    Instruction(OperationAsm.CMP, listOf(operands[0], Constant(0))),
                    Instruction(OperationAsm.JNE, listOf(dest[0])),
                    Instruction(OperationAsm.JMP, listOf(dest[1])),
                )
            },
            createEmptyExecPattern(UnaryOperationType.IF)
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(NullaryOperationType.RETURN, InstructionKind.EXEC, 1) { _, _ ->
                listOf(
                    Instruction(OperationAsm.RET, listOf())
                )
            }
        )
    }

    private fun createEmptyExecPattern(rootOperation: OperationType): InstructionPattern {
        return TemplatePattern(rootOperation, InstructionKind.EXEC, 1) { _, _ -> listOf() }
    }

    private fun verifyAssignable(dest: List<OperandArgumentType>, operation: String) {
        if (dest.size == 0) {
            throw IllegalArgumentException("Destination for value-returning ${operation} cannot be empty")
        }
        if (!(dest[0] is Assignable)) {
            throw IllegalArgumentException("Destination for value-returning ${operation} must be an assignable")
        }
    }

    private fun verifyLabels(dest: List<OperandArgumentType>, operation: String) {
        if (dest.size < 2 || !(dest[0] is Label && dest[1] is Label)) {
            throw IllegalArgumentException("Two destination labels must be provided for jumping ${operation}")
        }
    }

    private fun verifyNotLabels(operands: List<OperandArgumentType>, operation: String) {
        for (op in operands) {
            if (op is Label) {
                throw IllegalArgumentException("Operands for ${operation} cannot be labels")
            }
        }
    }
}
