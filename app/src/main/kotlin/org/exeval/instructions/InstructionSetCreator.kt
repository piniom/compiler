package org.exeval.instructions

import org.exeval.cfg.*

data class InstructionPatternMapKey(
    val treeRootType: TreeKind,
    val instructionKind: InstructionKind
)

class InstructionSetCreator {
    private val patterns: Map<InstructionPatternMapKey, List<InstructionPattern>>
    private val labelFactory : LabelInstructionFactory

    init {
        labelFactory = LabelInstructionFactory()
        patterns = initInstructionSet()
    }

    fun createInstructionSet(): Map<InstructionPatternMapKey, List<InstructionPattern>> = patterns


    // Private

    private fun initInstructionSet(): Map<InstructionPatternMapKey, List<InstructionPattern>> {
        return (
            createAssignmentPatterns()

            + createSafeSimple2ArgPatterns(BinaryAddKind, OperationAsm.ADD)
            + createSafeSimple2ArgPatterns(BinarySubtractKind, OperationAsm.SUB)

            + createMultiplyPatterns()
            + createDividePatterns()

            + createAndPatterns()
            + createOrPatterns()

            + createSimpleComparisonPatterns(BinaryGreaterKind, OperationAsm.CMOVG, OperationAsm.JG)
            + createSimpleComparisonPatterns(BinaryGreaterEqualKind, OperationAsm.CMOVGE, OperationAsm.JGE)
            + createSimpleComparisonPatterns(BinaryEqualKind, OperationAsm.CMOVE, OperationAsm.JE)

            + createNotPatterns()
            + createNegationPatterns()
            + createCallPatterns()

            + createReturnPatterns()
        ).groupBy{ InstructionPatternMapKey(it.rootType, it.kind) }
    }

    // TODO Are labels considered by assembly as "immediate values"?

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        /* NOTE Needed only if both operand virtual registers are mapped to memory.
         *      Then has to be a physical register, not memory.
         */
        val reg1 = VirtualRegister()

        return listOf(
            // NOTE Value of assignment is Nope, so it only exists in EXEC variant
            TemplatePattern(
                AssignmentKind,
                InstructionKind.EXEC,
                1
            ) { _, inputs, _ ->
                if (inputs.size != 2) {
                    throw IllegalArgumentException(
                        """Assignment takes exactly two arguments:
                        [1] destination (where to assign) and
                        [2] source (what to assign)""".trimIndent()
                    )
                }
                if (!(inputs is Register)) { // TODO or Memory
                    throw IllegalArgumentException(
                        "First argument for assignment must be a register or memory location"
                    )
                }
                if (false) { // TODO inputRegisters[0] is Memory && inputRegisters[1] is Memory
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[1])),
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(inputs[0], reg1))
                    )
                } else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(inputs[0], inputs[1]))
                    )
                }
            }
        )
    }

    private fun createMultiplyPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryMultiplyKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning multiply cannot be null"
                    )
                }
                if (inputs.size != 2) {
                    throw IllegalArgumentException("Multiply takes exactly two arguments")
                }
                createMulDivModInstructions(OperationAsm.MUL, dest, inputs)
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(BinaryMultiplyKind)
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryDivideKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning divide cannot be null"
                    )
                }
                if (inputs.size != 2) {
                    throw IllegalArgumentException("Divide takes exactly two arguments")
                }
                createMulDivModInstructions(OperationAsm.DIV, dest, inputs)
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(BinaryDivideKind)
        )
    }

    private fun createMulDivModInstructions(
        operation: OperationAsm,
        dest: VirtualRegister,
        inputs: List<OperandArgumentType>
    ): List<Instruction> {
        // NOTE Needed always, can be either register or memory
        val reg1 = VirtualRegister()
        // NOTE Needed only if second argument is a constant, can be either register or memory
        val reg2 = VirtualRegister()

        return listOf(
            // Save registers modified by assembly instruction
            SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, PhysicalRegister.RAX)),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(PhysicalRegister.RAX, inputs[0])),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, PhysicalRegister.RDX)),
        ) + if (inputs[1] is ConstantOperandArgumentType) {
            listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg2, inputs[1])),
                SimpleAsmInstruction(operation, listOf(reg2)),
            )
        }
        else {
            listOf(
                SimpleAsmInstruction(operation, listOf(inputs[1])),
            )
        } + listOf(
            // Save result & restore registers
            SimpleAsmInstruction(OperationAsm.XCHG, listOf(dest, PhysicalRegister.RAX)),
            SimpleAsmInstruction(OperationAsm.MOV, listOf(PhysicalRegister.RDX, reg1)),
        )
    }

    private fun createSafeSimple2ArgPatterns(
        rootOperation: TreeKind,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning ${rootOperation} cannot be null"
                    )
                }
                if (inputs.size != 2) {
                    throw IllegalArgumentException("${rootOperation} takes exactly two arguments")
                }
                listOf(
                    // TODO fix if both are memory
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputs[0]))
                ) + create2ArgInstruction(asmOperation, dest, inputs[1])
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun createAndPatterns(): List<InstructionPattern> {
        // NOTE Needed only if at least one argument is a constant, can be either register or memory
        val reg1 = VirtualRegister()

        return createSimpleBoolOperationPatterns(BinaryAndKind, OperationAsm.AND) + listOf(
            TemplatePattern(BinaryAndKind, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 2) {
                    throw IllegalArgumentException("Boolean and takes exactly two arguments")
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                var shortCutLabel = labelFactory.createLabel("ShortcutAndLabel")
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[0])),
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(reg1, NumericalConstant(0))),
                    )
                }
                else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(inputs[0], NumericalConstant(0)))
                    )
                } + listOf(
                    SimpleAsmInstruction(OperationAsm.JNE, listOf( shortCutLabel )),
                ) + if (inputs[1] is ConstantOperandArgumentType) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[1])),
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(reg1, NumericalConstant(0))),
                    )
                }
                else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(inputs[1], NumericalConstant(0)))
                    )
                } + listOf(
                    SimpleAsmInstruction(OperationAsm.JE, listOf( label )),
                    LabelDeclarationInstruction(shortCutLabel)
                )
            }
        )
    }

    private fun createOrPatterns(): List<InstructionPattern> {
        // NOTE Needed only if at least one argument is a constant, can be either register or memory
        val reg1 = VirtualRegister()

        return createSimpleBoolOperationPatterns(BinaryOrKind, OperationAsm.OR) + listOf(
            TemplatePattern(BinaryOrKind, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 2) {
                    throw IllegalArgumentException("Boolean or takes exactly two arguments")
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[0])),
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(reg1, NumericalConstant(0))),
                    )
                }
                else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(inputs[0], NumericalConstant(0)))
                    )
                } + listOf(
                    // TODO fix labels
                    SimpleAsmInstruction(OperationAsm.JNE, listOf( label )),
                ) + if (inputs[1] is ConstantOperandArgumentType) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[1])),
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(reg1, NumericalConstant(0))),
                    )
                }
                else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(inputs[1], NumericalConstant(0)))
                    )
                } + listOf(
                    SimpleAsmInstruction(OperationAsm.JNE, listOf( label )),
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
            SimpleAsmInstruction(OperationAsm.ADC, listOf(dest, NumericalConstant(0))),
            // If carry was set, dest will be equal to 1, otherwise it'll be 0
        )
    }

    private fun createSimpleBoolOperationPatterns(
        rootOperation: TreeKind,
        asmOperation: OperationAsm
    ): List<InstructionPattern> {
        // NOTE Needed always (for VALUE kind), must be a register
        val reg1 = VirtualRegister()

        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning boolean ${rootOperation} cannot be null"
                    )
                }
                if (inputs.size != 2) {
                    throw IllegalArgumentException(
                        "Boolean ${rootOperation} takes exactly two arguments"
                    )
                }
                convertBooleanTo0Or1(reg1, inputs[0]) + listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1))
                ) + convertBooleanTo0Or1(reg1, inputs[1]) +
                        create2ArgInstruction(asmOperation, dest, reg1)
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun createSimpleComparisonPatterns(
        rootOperation: TreeKind,
        asmCmovOperation: OperationAsm,
        asmJccOperation: OperationAsm
    ): List<InstructionPattern> {
        // NOTE Needed always in VALUE variant. Must be a register, not memory
        val reg1 = VirtualRegister()

        return listOf(
            TemplatePattern(rootOperation, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning comparison ${rootOperation} cannot be null"
                    )
                }
                if (inputs.size != 2) {
                    throw IllegalArgumentException(
                        "Comparision ${rootOperation} takes exactly two arguments"
                    )
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.XOR, listOf(reg1, reg1)),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, NumericalConstant(1))),
                ) + create2ArgInstruction(OperationAsm.CMP, inputs[0], inputs[1]) + listOf(
                    // NOTE The first operand HAS to be a register (cannot be memory)
                    SimpleAsmInstruction(asmCmovOperation, listOf(reg1, dest)),
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1)),
                )
            },
            TemplatePattern(rootOperation, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 2) {
                    throw IllegalArgumentException(
                        "Comparision ${rootOperation} takes exactly two arguments"
                    )
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                create2ArgInstruction(OperationAsm.CMP, inputs[0], inputs[1]) + listOf(
                    SimpleAsmInstruction(asmJccOperation, listOf( label )),
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun create2ArgInstruction(
        operation: OperationAsm,
        operand1: OperandArgumentType,
        operand2: OperandArgumentType
    ): List<Instruction> {
        /* NOTE Needed if:
         *      - both operands are memory, must be a register
         *      - first operand is constant and second is register, can be either register or memory
         *      - first operand is constant and second isn't register, must be a register
         */
        val reg1 = VirtualRegister()

        // TODO fix register/memory types
        return if (operand1 is Register) {
            listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
        }
        else if (operand1 is ConstantOperandArgumentType) {
            listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
        }
        else if (false /* operand2 is Memory */) {
            listOf(
                SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, operand2)),
                SimpleAsmInstruction(operation, listOf(operand1, reg1)),
            )
        }
        else {
            listOf(
                SimpleAsmInstruction(operation, listOf(operand1, operand2))
            )
        }
    }

    private fun createNotPatterns(): List<InstructionPattern> {
        // NOTE Needed only in JUMP variant if operand is a constant, can be either register or memory
        val reg1 = VirtualRegister()

        return listOf(
            TemplatePattern(UnaryNotKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning boolean negation cannot be null"
                    )
                }
                if (inputs.size != 1) {
                    throw IllegalArgumentException("Boolean negation takes exactly one argument")
                }
                listOf(
                    SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputs[0])),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannnot be a constant.
                     */
                    SimpleAsmInstruction(OperationAsm.SUB, listOf(dest, NumericalConstant(1))),
                    SimpleAsmInstruction(OperationAsm.NEG, listOf(dest))
                )
            },
            TemplatePattern(UnaryNotKind, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 1) {
                    throw IllegalArgumentException("Boolean negation takes exactly one argument")
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[0])),
                        SimpleAsmInstruction(OperationAsm.CMP, listOf(reg1, NumericalConstant(0))),
                    )
                }
                else {
                    listOf(
                    SimpleAsmInstruction(OperationAsm.CMP, listOf(inputs[0], NumericalConstant(0))),
                    )
                } + listOf(
                    // TODO fix labels
                    SimpleAsmInstruction(OperationAsm.JE, listOf( label )),
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(UnaryNotKind)
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        // NOTE Needed only if dest and input are both memory, has to be a register, not memory
        val reg1 = VirtualRegister()

        return listOf(
            TemplatePattern(UnaryMinusKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for negation cannot be null")
                }
                if (inputs.size != 1) {
                    throw IllegalArgumentException("Negation takes exactly one argument")
                }
                if (false /* dest is Memory && inputs[0] is Memory */) {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(reg1, inputs[0])),
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, reg1)),
                        SimpleAsmInstruction(OperationAsm.NEG, listOf(dest))
                    )
                }
                else {
                    listOf(
                        SimpleAsmInstruction(OperationAsm.MOV, listOf(dest, inputs[0])),
                        SimpleAsmInstruction(OperationAsm.NEG, listOf(dest))
                    )
                }
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(UnaryMinusKind)
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                CallKind,
                InstructionKind.EXEC,
                1
            ) { _, inputs, _ ->
                if (inputs.size != 1) {
                    throw IllegalArgumentException(
                        "Function call takes exactly one argument: address of function to be called"
                    )
                }
                listOf(
                    // The argument must contain the address or label where the target function is located
                    SimpleAsmInstruction(OperationAsm.CALL, listOf(inputs[0]))
                )
            }
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                ReturnKind,
                InstructionKind.VALUE,
                1
            ) { _, _, _ ->
                listOf(
                    SimpleAsmInstruction(OperationAsm.RET, listOf())
                )
            }
        )
    }

    private fun createEmptyExecPattern(rootType: TreeKind): TemplatePattern {
        return TemplatePattern(rootType, InstructionKind.EXEC, 1) { _, _ , _-> listOf() }
    }

    private class LabelDeclarationInstruction(private val label: Label) : Instruction {

        fun toAsm(mapping: Map<Register, PhysicalRegister>): String {
            return label.name + ":"
        }
    
        fun usedRegisters(): List<Register> {
            return listOf()
        }
    
        fun definedRegisters(): List<Register> {
            return listOf()
        }
    
        fun isCopy(): Boolean {
            return false
        }
    }
    
    private class LabelInstructionFactory {
        private var currentLabelNumber : Int = 0
    
        public fun createLabel(labelBaseName : String) : Label {
            currentLabelNumber += 1
            return Label(labelBaseName + currentLabelNumber.toString())
        }
    }
}
