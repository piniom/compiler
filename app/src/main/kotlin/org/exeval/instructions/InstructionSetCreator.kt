package org.exeval.instructions

import org.exeval.cfg.*

class InstructionSetCreator {
    private val patterns: Map<Any, List<InstructionPattern>>

    init {
        patterns = initInstructionSet()
    }

    fun createInstructionSet(): Map<Any, List<InstructionPattern>> = patterns


    // Private

    private fun initInstructionSet(): Map<Any, List<InstructionPattern>> {
        return mapOf(
			/*
            AssignmentTree::class to createAssignmentPatterns(),
            BinaryTreeOperationType.ADD to createSafeSimple2ArgPattern(
                BinaryTreeOperationType.ADD, OperationAsm.ADD
            ),
            BinaryTreeOperationType.SUBTRACT to createSafeSimple2ArgPattern(
                BinaryTreeOperationType.SUBTRACT, OperationAsm.SUB
            ),

            BinaryTreeOperationType.MULTIPLY to createMultiplyPatterns(),
            BinaryTreeOperationType.DIVIDE to createDividePatterns(),

            BinaryTreeOperationType.AND to createSimpleBoolOperationPattern(
                BinaryTreeOperationType.AND, OperationAsm.AND
            ),
            BinaryTreeOperationType.OR to createSimpleBoolOperationPattern(
                BinaryTreeOperationType.OR, OperationAsm.OR
            ),

            BinaryTreeOperationType.GREATER to createSimpleComparisonPattern(
                BinaryTreeOperationType.GREATER, OperationAsm.CMOVG
            ),
            BinaryTreeOperationType.GREATER_EQUAL to createSimpleComparisonPattern(
                BinaryTreeOperationType.GREATER_EQUAL, OperationAsm.CMOVGE
            ),
            BinaryTreeOperationType.EQUAL to createSimpleComparisonPattern(
                BinaryTreeOperationType.EQUAL, OperationAsm.CMOVE
            ),

            UnaryTreeOperationType.NOT to createNotPatterns(),
            UnaryTreeOperationType.MINUS to createNegationPatterns(),
            Call::class to createCallPatterns(),

            Return::class to createReturnPatterns(),
			*/
        )
    }

	/*
    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(AssignmentTree::class, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for assignment cannot be null")
                }
                if (destRegister is MemoryTree && operands[0] is MemoryTree) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */, operands[0])),
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */))
                    )
                } else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, operands[0]))
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

    private fun createMulDivModInstructions(
        operation: OperationAsm,
        operands: List<OperandArgumentTypeTree>,
        destRegister: AssignableTree
    ): List<Instruction> {
        return listOf(
            SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, PhysicalRegister.RAX)),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(PhysicalRegister.RAX, operands[0])),
            SimpleAsmInstruction(
                OperationAsm.MOV, listOf(
                    VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */,
                    PhysicalRegister.RDX
                )
            )
        ) + when (operands[1]) {
            // Case: Register or Memory
            is AssignableTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operands[1])),
            )
            // Case: Constant or Label
            is ConstantTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */, operands[1])),
                SimpleAsmInstruction(operation, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */)),
            )
			else -> throw IllegalArgumentException("Unknown type of operands")
        } + listOf(
            SimpleAsmInstruction(OperationAsm.XCHG, listOf(destRegister, PhysicalRegister.RAX)),
            SimpleAsmInstruction(
                OperationAsm.XCHG, listOf(
                    PhysicalRegister.RDX,
                    VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */
                )
            ),
        )
    }

    private fun createSafeSimple2ArgPattern(
        rootOperation: BinaryTreeOperationType,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for 2-argument operation ${rootOperation} cannot be null")
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, operands[0]))
                ) + create2ArgInstruction(asmOperation, destRegister, operands[1])
            }
        )
    }

    private fun convertBooleanTo0Or1(
        destRegisterTree: VirtualRegister, //RegisterTree,
        boolean: OperandArgumentTypeTree
    ): List<Instruction> {
        return listOf(
            // A neat conversion without jumps found on stackoverflow

            // Set destRegister to 0
            SimpleAsmInstruction(OperationAsm.XOR, listOf(destRegisterTree, destRegisterTree)),
            // Carry will be set if boolean was not 0
            SimpleAsmInstruction(OperationAsm.SUB, listOf(destRegisterTree, boolean)),
            // Set destRegister to 0 once again
            SimpleAsmInstruction(OperationAsm.XOR, listOf(destRegisterTree, destRegisterTree)),
            // Add carry to destRegister + 0
            SimpleAsmInstruction(OperationAsm.ADC, listOf(destRegisterTree, NumericalConstantTree(0))),
            // If carry was set, destRegister will be equal to 1, otherwise it'll be 0
        )
    }

    private fun createSimpleBoolOperationPattern(
        rootOperation: BinaryTreeOperationType,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for 2-argument boolean operation ${rootOperation} cannot be null")
                }
                convertBooleanTo0Or1(VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */, operands[0]) + listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */))
                ) + convertBooleanTo0Or1(VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */, operands[1]) +
                        create2ArgInstruction(asmOperation, destRegister, VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */)
            }
        )
    }

    private fun createSimpleComparisonPattern(
        rootOperation: BinaryTreeOperationType,
        asmCmovOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister == null) {
                    throw IllegalArgumentException("Destination register for value-returning comparison cannot be null")
                }
                listOf(
                    SimpleAsmInstruction(
                        OperationAsm.XOR, listOf(
                            VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */,
                            VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */
                        )
                    ),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, NumericalConstantTree(1))),
                ) + create2ArgInstruction(OperationAsm.CMP, operands[0], operands[1]) + listOf(
                    // The first operand HAS to be a register (cannot be memory)
                    SimpleAsmInstruction(asmCmovOperation, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */, destRegister)),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister() /* VirtualRegister(WorkingRegisters.R1) */)),
                )
            }
        )
    }

    private fun create2ArgInstruction(
        operation: OperationAsm,
        operand1: OperandArgumentTypeTree,
        operand2: OperandArgumentTypeTree
    ): List<Instruction> {
        return when {
            // Case: Register + Register
            operand1 is RegisterTree && operand2 is RegisterTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Register + Memory
            operand1 is RegisterTree && operand2 is MemoryTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Register
            operand1 is MemoryTree && operand2 is RegisterTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Memory
            operand1 is MemoryTree && operand2 is MemoryTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */)),
            )
            // Case: Register + Constant
            operand1 is RegisterTree && operand2 is ConstantTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Register
            operand1 is ConstantTree && operand2 is RegisterTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */)),
            )
            // Case: Memory + Constant
            operand1 is MemoryTree && operand2 is ConstantTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Memory
            operand1 is ConstantTree && operand2 is MemoryTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */)),
            )
            // Case: Constant + Constant
            operand1 is ConstantTree && operand2 is ConstantTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, VirtualRegister() /* VirtualRegister(WorkingRegisters.R0) */)),
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
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannnot be a constant.
                     */
                    SimpleAsmInstruction(OperationAsm.SUB, listOf(destRegister, NumericalConstantTree(1))),
                    SimpleAsmInstruction(OperationAsm.NEG, listOf(destRegister))
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
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(destRegister, operands[0])),
                    SimpleAsmInstruction(OperationAsm.NEG, listOf(destRegister))
                )
            }
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(Call::class, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    // The argument must contain the address or label where the target function is located
                    SimpleAsmInstruction(OperationAsm.CALL, listOf(operands[0]))
                )
            }
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(Return::class, InstructionKind.VALUE, 1) { operands, destRegister ->
                listOf(
                    SimpleAsmInstruction(OperationAsm.RET, listOf())
                )
            }
        )
    }
	*/
}
