package org.exeval.instructions

import org.exeval.cfg.*

data class InstructionPatternMapKey(
    val treeRootType: InstructionPatternRootType,
    val instructionKind: InstructionKind
)

class InstructionSetCreator {
    private val patterns: Map<InstructionPatternMapKey, List<InstructionPattern>>

    init {
        patterns = initInstructionSet()
    }

    fun createInstructionSet(): Map<InstructionPatternMapKey, List<InstructionPattern>> = patterns


    // Private

    private fun initInstructionSet(): Map<InstructionPatternMapKey, List<InstructionPattern>> {
        return (
            createAssignmentPatterns()

            + createSafeSimple2ArgPattern(BinaryTreeOperationType.ADD, OperationAsm.ADD)
            + createSafeSimple2ArgPattern(BinaryTreeOperationType.SUBTRACT, OperationAsm.SUB)

            + createMultiplyPatterns()
            + createDividePatterns()

            + createSimpleBoolOperationPattern(BinaryTreeOperationType.AND, OperationAsm.AND)
            + createSimpleBoolOperationPattern(BinaryTreeOperationType.OR, OperationAsm.OR)

            + createSimpleComparisonPattern(BinaryTreeOperationType.GREATER, OperationAsm.CMOVG)
            + createSimpleComparisonPattern(BinaryTreeOperationType.GREATER_EQUAL, OperationAsm.CMOVGE)
            + createSimpleComparisonPattern(BinaryTreeOperationType.EQUAL, OperationAsm.CMOVE)

            + createNotPatterns()
            + createNegationPatterns()
            + createCallPatterns()

            + createReturnPatterns()
        ).groupBy{ InstructionPatternMapKey(it.rootType, it.kind) }
    }

    // TODO inputRegisters is never a MemoryTree - needed a way to know if was mapped to memory or register
    // TODO inputRegisters is never a ConstantTree - inject constants at matching time

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(AssignmentTree::class, null),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for assignment cannot be null")
                }
                val reg1 = VirtualRegister()
                @Suppress("USELESS_IS_CHECK")
                if (dest is MemoryTree && inputRegisters[0] is MemoryTree) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputRegisters[0])),
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1))
                    )
                } else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputRegisters[0]))
                    )
                }
            }
        )
    }

    private fun createMultiplyPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    BinaryOperationTree::class,
                    BinaryTreeOperationType.MULTIPLY
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for multiply cannot be null")
                }
                @Suppress("USELESS_IS_CHECK")
                if (!(dest is AssignableTree)) {
                    throw IllegalArgumentException("Destination for multiply must be an assignable")
                }
                createMulDivModInstructions(OperationAsm.MUL, dest, inputRegisters)
            }
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    BinaryOperationTree::class,
                    BinaryTreeOperationType.DIVIDE
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for divide cannot be null")
                }
                @Suppress("USELESS_IS_CHECK")
                if (!(dest is AssignableTree)) {
                    throw IllegalArgumentException("Destination for divide must be an assignable")
                }
                createMulDivModInstructions(OperationAsm.DIV, dest, inputRegisters)
            }
        )
    }

    private fun createMulDivModInstructions(
        operation: OperationAsm,
        dest: VirtualRegister,
        inputRegisters: List<VirtualRegister>
    ): List<Instruction> {
        val reg1 = VirtualRegister()
        val reg2 = VirtualRegister()
        return listOf(
            SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, PhysicalRegister.RAX)),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(PhysicalRegister.RAX, inputRegisters[0])),
            SimpleAsmInstruction(
                OperationAsm.MOV, listOf(
                    reg1,
                    PhysicalRegister.RDX
                )
            )
        ) +
        @Suppress("USELESS_IS_CHECK")
        when (inputRegisters[1]) {
            // Case: Register or Memory
            // @Suppress("USELESS_IS_CHECK")
            is AssignableTree -> listOf(
                SimpleAsmInstruction(operation, listOf(inputRegisters[1])),
            )
            // Case: Constant or Label
            // @Suppress("USELESS_IS_CHECK")
            is ConstantTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg2, inputRegisters[1])),
                SimpleAsmInstruction(operation, listOf(reg2)),
            )
            else -> throw IllegalArgumentException("Unknown type of operands")
        } + listOf(
            SimpleAsmInstruction(OperationAsm.XCHG, listOf(dest, PhysicalRegister.RAX)),
            SimpleAsmInstruction(
                OperationAsm.XCHG, listOf(
                    PhysicalRegister.RDX,
                    reg1
                )
            ),
        )
    }

    private fun createSafeSimple2ArgPattern(
        rootOperation: BinaryTreeOperationType,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    BinaryOperationTree::class,
                    rootOperation
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for 2-argument operation ${rootOperation} cannot be null")
                }
                @Suppress("USELESS_IS_CHECK")
                if (!(dest is AssignableTree)) {
                    throw IllegalArgumentException("Destination for 2-argument operation ${rootOperation} must be an assignable")
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputRegisters[0]))
                ) + create2ArgInstruction(asmOperation, dest, inputRegisters[1])
            }
        )
    }

    private fun convertBooleanTo0Or1(
        dest: VirtualRegister,
        boolean: OperandArgumentTypeTree
    ): List<Instruction> {
        return listOf(
            // A neat conversion without jumps found on stackoverflow

            // Set dest to 0
            SimpleAsmInstruction(OperationAsm.XOR, listOf(dest, dest)),
            // Carry will be set if boolean was not 0
            SimpleAsmInstruction(OperationAsm.SUB, listOf(dest, boolean)),
            // Set dest to 0 once again
            SimpleAsmInstruction(OperationAsm.XOR, listOf(dest, dest)),
            // Add carry to dest + 0
            SimpleAsmInstruction(OperationAsm.ADC, listOf(dest, 0)),
            // If carry was set, dest will be equal to 1, otherwise it'll be 0
        )
    }

    private fun createSimpleBoolOperationPattern(
        rootOperation: BinaryTreeOperationType,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    BinaryOperationTree::class,
                    rootOperation
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for 2-argument boolean operation ${rootOperation} cannot be null")
                }
                val reg1 = VirtualRegister()
                convertBooleanTo0Or1(reg1, inputRegisters[0]) + listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1))
                ) + convertBooleanTo0Or1(reg1, inputRegisters[1]) +
                        create2ArgInstruction(asmOperation, dest, reg1)
            }
        )
    }

    private fun createSimpleComparisonPattern(
        rootOperation: BinaryTreeOperationType,
        asmCmovOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    BinaryOperationTree::class,
                    rootOperation
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for value-returning comparison cannot be null")
                }
                @Suppress("USELESS_IS_CHECK")
                if (!(dest is AssignableTree)) {
                    throw IllegalArgumentException("Destination for value-returning comparison must be an assignable")
                }
                val reg1 = VirtualRegister()
                listOf(
                    SimpleAsmInstruction(OperationAsm.XOR, listOf(reg1, reg1)
                    ),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, 1)),
                ) + create2ArgInstruction(OperationAsm.CMP, inputRegisters[0], inputRegisters[1]) + listOf(
                    // The first operand HAS to be a register (cannot be memory)
                    SimpleAsmInstruction(asmCmovOperation, listOf(reg1, dest)),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1)),
                )
            }
        )
    }

    private fun create2ArgInstruction(
        operation: OperationAsm,
        operand1: OperandArgumentTypeTree,
        operand2: OperandArgumentTypeTree
    ): List<Instruction> {
        val reg1 = VirtualRegister()
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
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            // Case: Register + Constant
            operand1 is RegisterTree && operand2 is ConstantTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Register
            operand1 is ConstantTree && operand2 is RegisterTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            // Case: Memory + Constant
            operand1 is MemoryTree && operand2 is ConstantTree -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Memory
            operand1 is ConstantTree && operand2 is MemoryTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            // Case: Constant + Constant
            operand1 is ConstantTree && operand2 is ConstantTree -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )

            else -> throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
        }
    }

    private fun createNotPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    UnaryOperationTree::class,
                    UnaryTreeOperationType.NOT
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for boolean negation cannot be null")
                }
                @Suppress("USELESS_IS_CHECK")
                if (!(dest is AssignableTree)) {
                    throw IllegalArgumentException("Destination for boolean negation must be an assignable")
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputRegisters[0])),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannnot be a constant.
                     */
                    SimpleAsmInstruction(OperationAsm.SUB, listOf(dest, 1)),
                    SimpleAsmInstruction(OperationAsm.NEG, listOf(dest))
                )
            }
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(
                    UnaryOperationTree::class,
                    UnaryTreeOperationType.MINUS
                ),
                InstructionKind.VALUE,
                1
            ) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for negation cannot be null")
                }
                @Suppress("USELESS_IS_CHECK")
                if (!(dest is AssignableTree)) {
                    throw IllegalArgumentException("Destination for negation must be an assignable")
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputRegisters[0])),
                    SimpleAsmInstruction(OperationAsm.NEG, listOf(dest))
                )
            }
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(Call::class, null),
                InstructionKind.VALUE,
                1
            ) { _, inputRegisters ->
                listOf(
                    // The argument must contain the address or label where the target function is located
                    SimpleAsmInstruction(OperationAsm.CALL, listOf(inputRegisters[0]))
                )
            }
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(Return::class, null),
                InstructionKind.VALUE,
                1
            ) { _, _ ->
                listOf(
                    SimpleAsmInstruction(OperationAsm.RET, listOf())
                )
            }
        )
    }
}
