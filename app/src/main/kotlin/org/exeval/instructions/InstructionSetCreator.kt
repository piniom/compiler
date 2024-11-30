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

            + createSafeSimple2ArgPatterns(BinaryTreeOperationType.ADD, OperationAsm.ADD)
            + createSafeSimple2ArgPatterns(BinaryTreeOperationType.SUBTRACT, OperationAsm.SUB)

            + createMultiplyPatterns()
            + createDividePatterns()

            + createAndPatterns()
            + createOrPatterns()

            + createSimpleComparisonPatterns(BinaryTreeOperationType.GREATER, OperationAsm.CMOVG, OperationAsm.JG)
            + createSimpleComparisonPatterns(BinaryTreeOperationType.GREATER_EQUAL, OperationAsm.CMOVGE, OperationAsm.JGE)
            + createSimpleComparisonPatterns(BinaryTreeOperationType.EQUAL, OperationAsm.CMOVE, OperationAsm.JE)

            + createNotPatterns()
            + createNegationPatterns()
            + createCallPatterns()

            + createReturnPatterns()
        ).groupBy{ InstructionPatternMapKey(it.rootType, it.kind) }
    }

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            // NOTE Value of assignment is Nope, so it only exists in EXEC variant
            TemplatePattern(
                InstructionPatternRootType(AssignmentTree::class, null),
                InstructionKind.EXEC,
                1
            ) { _, inputRegisters ->
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException(
                        """Assignment takes exactly two arguments:
                        [1] destination (where to assign) and
                        [2] source (what to assign)""".trimIndent()
                    )
                }
                val reg1 = VirtualRegister()
                if (false) { // TODO inputRegisters[0] is Memory && inputRegisters[1] is Memory
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputRegisters[1])),
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(inputRegisters[0], reg1))
                    )
                } else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(inputRegisters[0], inputRegisters[1]))
                    )
                }
            }
        )
    }

    private fun createMultiplyPatterns(): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            BinaryTreeOperationType.MULTIPLY
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning multiply cannot be null"
                    )
                }
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("Multiply takes exactly two arguments")
                }
                createMulDivModInstructions(OperationAsm.MUL, dest, inputRegisters)
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            BinaryTreeOperationType.DIVIDE
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning divide cannot be null"
                    )
                }
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("Divide takes exactly two arguments")
                }
                createMulDivModInstructions(OperationAsm.DIV, dest, inputRegisters)
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun createMulDivModInstructions(
        operation: OperationAsm,
        dest: VirtualRegister,
        inputRegisters: List<OperandArgumentType>
    ): List<Instruction> {
        val reg1 = VirtualRegister()
        val reg2 = VirtualRegister()
        return listOf(
            // Save registers modified by assembly instruction
            SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, PhysicalRegister.RAX)),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(PhysicalRegister.RAX, inputRegisters[0])),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, PhysicalRegister.RDX)),
        ) + if (false) { // TODO inputRegisters[1] is Constant or Label
            listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg2, inputRegisters[1])),
                SimpleAsmInstruction(operation, listOf(reg2)),
            )
        }
        else {
            listOf(
                SimpleAsmInstruction(operation, listOf(inputRegisters[1])),
            )
        } + listOf(
            // Save result & restore registers
            SimpleAsmInstruction(OperationAsm.XCHG, listOf(dest, PhysicalRegister.RAX)),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(PhysicalRegister.RDX, reg1)),
        )
    }

    private fun createSafeSimple2ArgPatterns(
        rootOperation: BinaryTreeOperationType,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            rootOperation
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning ${rootOperation} cannot be null"
                    )
                }
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("${rootOperation} takes exactly two arguments")
                }
                listOf(
                    // TODO fix if both are memory
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputRegisters[0]))
                ) + create2ArgInstruction(asmOperation, dest, inputRegisters[1])
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun createAndPatterns(): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            BinaryTreeOperationType.AND
        )
        return createSimpleBoolOperationPatterns(BinaryTreeOperationType.AND, OperationAsm.AND) + listOf(
            TemplatePattern(rootType, InstructionKind.JUMP, 1) { dest, inputRegisters ->
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("Boolean and takes exactly two arguments")
                }
                // TODO fix; where to get labels from?
                listOf(
                    SimpleAsmInstruction(OperationAsm.CMP, listOf(inputRegisters[0], 0)),
                    SimpleAsmInstruction(OperationAsm.JE, listOf(dest!! /* label-false */ )),
                    SimpleAsmInstruction(OperationAsm.CMP, listOf(inputRegisters[1], 0)),
                    SimpleAsmInstruction(OperationAsm.JE, listOf(dest!! /* label-false */ )),
                    SimpleAsmInstruction(OperationAsm.JMP, listOf(dest!! /* label-true */ )),
                )
            }
        )
    }

    private fun createOrPatterns(): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            BinaryTreeOperationType.OR
        )
        return createSimpleBoolOperationPatterns(BinaryTreeOperationType.OR, OperationAsm.OR) + listOf(
            TemplatePattern(rootType, InstructionKind.JUMP, 1) { dest, inputRegisters ->
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("Boolean or takes exactly two arguments")
                }
                // TODO fix; where to get labels from?
                listOf(
                    SimpleAsmInstruction(OperationAsm.CMP, listOf(inputRegisters[0], 0)),
                    SimpleAsmInstruction(OperationAsm.JNE, listOf(dest!! /* label-true */ )),
                    SimpleAsmInstruction(OperationAsm.CMP, listOf(inputRegisters[1], 0)),
                    SimpleAsmInstruction(OperationAsm.JNE, listOf(dest!! /* label-true */ )),
                    SimpleAsmInstruction(OperationAsm.JMP, listOf(dest!! /* label-false */ )),
                )
            }
        )
    }

    private fun convertBooleanTo0Or1(
        dest: VirtualRegister,
        boolean: OperandArgumentType
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

    private fun createSimpleBoolOperationPatterns(
        rootOperation: BinaryTreeOperationType,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            rootOperation
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for value-returning boolean ${rootOperation} cannot be null")
                }
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("Boolean ${rootOperation} takes exactly two arguments")
                }
                val reg1 = VirtualRegister()
                convertBooleanTo0Or1(reg1, inputRegisters[0]) + listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1))
                ) + convertBooleanTo0Or1(reg1, inputRegisters[1]) +
                        create2ArgInstruction(asmOperation, dest, reg1)
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun createSimpleComparisonPatterns(
        rootOperation: BinaryTreeOperationType,
        asmCmovOperation: OperationAsm,
        asmJccOperation: OperationAsm
    ): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            BinaryOperationTree::class,
            rootOperation
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for value-returning comparison ${rootOperation} cannot be null")
                }
                if (inputRegisters.size != 2) {
                    throw IllegalArgumentException("Comparision ${rootOperation} takes exactly two arguments")
                }
                val reg1 = VirtualRegister()
                listOf(
                    SimpleAsmInstruction(OperationAsm.XOR, listOf(reg1, reg1)),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, 1)),
                ) + create2ArgInstruction(OperationAsm.CMP, inputRegisters[0], inputRegisters[1]) + listOf(
                    // The first operand HAS to be a register (cannot be memory)
                    SimpleAsmInstruction(asmCmovOperation, listOf(reg1, dest)),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1)),
                )
            },
            TemplatePattern(rootType, InstructionKind.JUMP, 1) { dest, inputRegisters ->
                // TODO fix; where to get labels from?
                create2ArgInstruction(OperationAsm.CMP, inputRegisters[0], inputRegisters[1]) + listOf(
                    SimpleAsmInstruction(asmJccOperation, listOf(dest!! /* label-true */ )),
                    SimpleAsmInstruction(OperationAsm.JMP, listOf(dest!! /* label-false */ ))
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun create2ArgInstruction(
        operation: OperationAsm,
        operand1: OperandArgumentType,
        operand2: OperandArgumentType
    ): List<Instruction> {
        val reg1 = VirtualRegister()
        @Suppress("USELESS_IS_CHECK")
        return when {
            // Case: Register + Register
            operand1 is Register && operand2 is Register -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // TODO fix types
            /*
            // Case: Register + Memory
            operand1 is Register && operand2 is Memory -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Register
            operand1 is Memory && operand2 is Register -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Memory + Memory
            operand1 is Memory && operand2 is Memory -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            // Case: Register + Constant
            operand1 is Register && operand2 is Constant -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Register
            operand1 is Constant && operand2 is Register -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            // Case: Memory + Constant
            operand1 is Memory && operand2 is Constant -> listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Memory
            operand1 is Constant && operand2 is Memory -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            // Case: Constant + Constant
            operand1 is Constant && operand2 is Constant -> listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
            */

            else -> throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
        }
    }

    private fun createNotPatterns(): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            UnaryOperationTree::class,
            UnaryTreeOperationType.NOT
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for value-returning boolean negation cannot be null")
                }
                if (inputRegisters.size != 1) {
                    throw IllegalArgumentException("Boolean negation takes exactly one argument")
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
            },
            TemplatePattern(rootType, InstructionKind.JUMP, 1) { dest, inputRegisters ->
                // TODO fix; where to get labels from?
                listOf(
                    SimpleAsmInstruction(OperationAsm.CMP, listOf(inputRegisters[0], 0)),
                    SimpleAsmInstruction(OperationAsm.JE, listOf(dest!! /* label-true */ )),
                    SimpleAsmInstruction(OperationAsm.JMP, listOf(dest!! /* label-false */ )),
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        val rootType = InstructionPatternRootType(
            UnaryOperationTree::class,
            UnaryTreeOperationType.MINUS
        )
        return listOf(
            TemplatePattern(rootType, InstructionKind.VALUE, 1) { dest, inputRegisters ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for negation cannot be null")
                }
                if (inputRegisters.size != 1) {
                    throw IllegalArgumentException("Negation takes exactly one argument")
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputRegisters[0])),
                    SimpleAsmInstruction(OperationAsm.NEG, listOf(dest))
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootType)
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                InstructionPatternRootType(Call::class, null),
                InstructionKind.EXEC,
                1
            ) { _, inputRegisters ->
                if (inputRegisters.size != 1) {
                    throw IllegalArgumentException("Function call takes exactly one argument: address of function to be called")
                }
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

    private fun createEmptyExecPattern(rootType: InstructionPatternRootType): TemplatePattern {
        return TemplatePattern(rootType, InstructionKind.EXEC, 1) { _, _ -> listOf() }
    }
}
