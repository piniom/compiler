package org.exeval.instructions

import org.exeval.cfg.*
import org.exeval.cfg.constants.*

class InstructionSetCreator {
    private val patterns: Map<TreeOperationType, List<InstructionPattern>>

    init {
        patterns = initInstructionSet()
    }

    fun createInstructionSet(): Map<TreeOperationType, List<InstructionPattern>> = patterns


    // Private

    private fun initInstructionSet(): Map<TreeOperationType, List<InstructionPattern>> {
        return mapOf(
            BinaryTreeOperationType.ASSIGNMENT to createAssignmentPatterns(),
            BinaryTreeOperationType.ADD to createSafeSimple2ArgPattern(
                BinaryTreeOperationType.ADD, OperationAsm.ADD),
            BinaryTreeOperationType.SUBTRACT to createSafeSimple2ArgPattern(
                BinaryTreeOperationType.SUBTRACT, OperationAsm.SUB),

            BinaryTreeOperationType.MULTIPLY to createMultiplyPatterns(),
            BinaryTreeOperationType.DIVIDE to createDividePatterns(),
            BinaryTreeOperationType.MODULO to createModuloPatterns(),

            BinaryTreeOperationType.AND to createSimpleBoolOperationPattern(
                BinaryTreeOperationType.AND, OperationAsm.AND),
            BinaryTreeOperationType.OR to createSimpleBoolOperationPattern(
                BinaryTreeOperationType.OR, OperationAsm.OR),
            BinaryTreeOperationType.XOR to createSimpleBoolOperationPattern(
                BinaryTreeOperationType.XOR, OperationAsm.XOR),

            BinaryTreeOperationType.GREATER to createSimpleComparisonPattern(
                BinaryTreeOperationType.GREATER, OperationAsm.CMOVG),
            BinaryTreeOperationType.GREATER_EQUAL to createSimpleComparisonPattern(
                BinaryTreeOperationType.GREATER_EQUAL, OperationAsm.CMOVGE),
            BinaryTreeOperationType.EQUAL to createSimpleComparisonPattern(
                BinaryTreeOperationType.EQUAL, OperationAsm.CMOVE),

            UnaryTreeOperationType.NOT to createNotPatterns(),
            UnaryTreeOperationType.MINUS to createNegationPatterns(),
            UnaryTreeOperationType.CALL to createCallPatterns(),

            NullaryTreeOperationType.RETURN to createReturnPatterns(),
        )
    }

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryTreeOperationType.ASSIGNMENT, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for assignment cannot be null")
                }
                if (destRegister is MemoryTree && operands[0] is MemoryTree) {
                    listOf(
                        Instruction(OperationAsm.MOV, listOf(VirtualRegisterTree(WorkingRegisters.R0), operands[0])),
                        Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegisterTree(WorkingRegisters.R0)))
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
            TemplatePattern(BinaryTreeOperationType.MULTIPLY, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for multiply cannot be null")
                }
                createMulDivModInstructions(OperationAsm.MUL, operands, destRegister)
            }
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryTreeOperationType.DIVIDE, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for divide cannot be null")
                }
                createMulDivModInstructions(OperationAsm.DIV, operands, destRegister)
            }
        )
    }

    private fun createModuloPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryTreeOperationType.MODULO, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for modulo cannot be null")
                }
                createMulDivModInstructions(OperationAsm.DIV, operands, destRegister) + listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegisterTree(WorkingRegisters.R0))),
                )
            }
        )
    }

    private fun createMulDivModInstructions(operation: OperationAsm, operands: List<OperandArgumentTypeTree>, destRegister: AssignableTree): List<Instruction> {
        return listOf(
            Instruction(OperationAsm.MOV, listOf(destRegister, PhysicalRegisterTree(Registers.RAX))),
            Instruction(OperationAsm.MOV, listOf(PhysicalRegisterTree(Registers.RAX), operands[0])),
            Instruction(OperationAsm.MOV, listOf(
                VirtualRegisterTree(WorkingRegisters.R0),
                PhysicalRegisterTree(Registers.RDX)
            ))
        ) + when (operands[1]) {
            // Case: Register or Memory
            is AssignableTree -> listOf(
                Instruction(operation, listOf(operands[1])),
            )
            // Case: Constant
            is NumericalConstantTree -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegisterTree(WorkingRegisters.R1), operands[1])),
                Instruction(operation, listOf(VirtualRegisterTree(WorkingRegisters.R1))),
            )
        } + listOf(
            Instruction(OperationAsm.XCHG, listOf(destRegister, PhysicalRegisterTree(Registers.RAX))),
            Instruction(OperationAsm.XCHG, listOf(
                PhysicalRegisterTree(Registers.RDX),
                VirtualRegisterTree(WorkingRegisters.R0)
            )),
        )
    }

    private fun createSafeSimple2ArgPattern(rootOperation: BinaryTreeOperationType, asmOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for 2-argument operation ${rootOperation} cannot be null")
                }
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0]))
                ) + create2ArgInstruction(asmOperation, destRegister, operands[1])
            }
        )
    }

    private fun convertBooleanTo0Or1(destRegisterTree: RegisterTree, boolean: OperandArgumentTypeTree): List<Instruction> {
        return listOf(
            // A neat conversion without jumps found on stackoverflow

            // Set destRegister to 0
            Instruction(OperationAsm.XOR, listOf(destRegisterTree, destRegisterTree)),
            // Carry will be set if boolean was not 0
            Instruction(OperationAsm.SUB, listOf(destRegisterTree, boolean)),
            // Set destRegister to 0 once again
            Instruction(OperationAsm.XOR, listOf(destRegisterTree, destRegisterTree)),
            // Add carry to destRegister + 0
            Instruction(OperationAsm.ADC, listOf(destRegisterTree, NumericalConstantTree(0))),
            // If carry was set, destRegister will be equal to 1, otherwise it'll be 0
        )
    }

    private fun createSimpleBoolOperationPattern(rootOperation: BinaryTreeOperationType, asmOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for 2-argument boolean operation ${rootOperation} cannot be null")
                }
                convertBooleanTo0Or1(VirtualRegisterTree(WorkingRegisters.R1), operands[0]) + listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegisterTree(WorkingRegisters.R1)))
                ) + convertBooleanTo0Or1(VirtualRegisterTree(WorkingRegisters.R1), operands[1]) +
                create2ArgInstruction(asmOperation, destRegister, VirtualRegisterTree(WorkingRegisters.R1))
            }
        )
    }

    private fun createSimpleComparisonPattern(rootOperation: BinaryTreeOperationType, asmCmovOperation: OperationAsm): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for value-returning comparison cannot be null")
                }
                listOf(
                    Instruction(OperationAsm.XOR, listOf(
                        VirtualRegisterTree(WorkingRegisters.R1),
                        VirtualRegisterTree(WorkingRegisters.R1))
                    ),
                    Instruction(OperationAsm.MOV, listOf(destRegister, NumericalConstantTree(1))),
                ) + create2ArgInstruction(OperationAsm.CMP, operands[0], operands[1]) + listOf(
                    // The first operand HAS to be a register (cannot be memory)
                    Instruction(asmCmovOperation, listOf(VirtualRegisterTree(WorkingRegisters.R1), destRegister)),
                    Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegisterTree(WorkingRegisters.R1))),
                )
            }
        )
    }

    private fun create2ArgInstruction(operation: OperationAsm, operand1: OperandArgumentTypeTree, operand2: OperandArgumentTypeTree): List<Instruction> {
        return when {
            // Case: Register + Register
            operand1 is RegisterTree && operand2 is RegisterTree -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Register + Memory
            operand1 is RegisterTree && operand2 is MemoryTree -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Register
            operand1 is MemoryTree && operand2 is RegisterTree -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Memory
            operand1 is MemoryTree && operand2 is MemoryTree -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegisterTree(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegisterTree(WorkingRegisters.R0))),
            )
            // Case: Register + Constant
            operand1 is RegisterTree && operand2 is NumericalConstantTree -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Register
            operand1 is NumericalConstantTree && operand2 is RegisterTree -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegisterTree(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegisterTree(WorkingRegisters.R0))),
            )
            // Case: Memory + Constant
            operand1 is MemoryTree && operand2 is NumericalConstantTree -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Memory
            operand1 is NumericalConstantTree && operand2 is MemoryTree -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegisterTree(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegisterTree(WorkingRegisters.R0))),
            )
            // Case: Constant + Constant
            operand1 is NumericalConstantTree && operand2 is NumericalConstantTree -> listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegisterTree(WorkingRegisters.R0), operand2)),
                Instruction(operation, listOf(operand1, VirtualRegisterTree(WorkingRegisters.R0))),
            )
            else -> throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
        }
    }

    private fun createNotPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryTreeOperationType.NOT, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for boolean negation cannot be null")
                }
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannnot be a constant.
                     */
                    Instruction(OperationAsm.SUB, listOf(destRegister, NumericalConstantTree(1))),
                    Instruction(OperationAsm.NEG, listOf(destRegister))
                )
            }
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryTreeOperationType.MINUS, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for negation cannot be null")
                }
                listOf(
                    Instruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    Instruction(OperationAsm.NEG, listOf(destRegister))
                )
            }
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(UnaryTreeOperationType.CALL, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    // The argument must contain the address or label where the target function is located
                    Instruction(OperationAsm.CALL, listOf(operands[0]))
                )
            }
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(NullaryTreeOperationType.RETURN, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    Instruction(OperationAsm.RET, listOf())
                )
            }
        )
    }
}
