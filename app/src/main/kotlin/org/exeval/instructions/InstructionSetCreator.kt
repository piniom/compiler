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
            + createSafeSimple2ArgPatterns(
                BinaryAddTreeKind,
                {par1: OperandArgumentType, par2: OperandArgumentType -> AddInstruction(par1 as AssignableDest, par2)}
            )
            + createSafeSimple2ArgPatterns(
                BinarySubtractTreeKind,
                {par1: OperandArgumentType, par2: OperandArgumentType -> SubInstruction(par1 as AssignableDest, par2)}
            )
            + createMultiplyPatterns()
            + createDividePatterns()

            + createAndPatterns()
            + createOrPatterns()

            + createSimpleComparisonPatterns(BinaryGreaterTreeKind, OperationAsm.CMOVG, OperationAsm.JG)
            + createSimpleComparisonPatterns(BinaryGreaterEqualTreeKind, OperationAsm.CMOVGE, OperationAsm.JGE)
            + createSimpleComparisonPatterns(BinaryEqualTreeKind, OperationAsm.CMOVE, OperationAsm.JE)
            + createSimpleComparisonPatterns(BinaryLessTreeKind, OperationAsm.CMOVL, OperationAsm.JL)
            + createSimpleComparisonPatterns(BinaryLessEqualTreeKind, OperationAsm.CMOVLE, OperationAsm.JLE)

            + createNotPatterns()
            + createNegationPatterns()
            + createCallPatterns()

            + createReturnPatterns()

            + createPushPatterns()
            + createPopPatterns()

            + createMemoryReadPatterns()
        ).groupBy{ InstructionPatternMapKey(it.rootType, it.kind) }
    }

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        /* NOTE Needed only if both operand virtual registers are mapped to memory.
         *      Then has to be a physical register, not memory.
         */
        val reg1 = VirtualRegister()

        return listOf(
            // NOTE Value of assignment is Nope, so it only exists in EXEC variant
            TemplatePattern(
                AssignmentTreeKind,
                InstructionKind.EXEC,
                1
            ) { dest, inputs, _ ->
                if (inputs.size != 1) {
                    throw IllegalArgumentException(
                        """Assignment takes exactly two arguments:
                        [1] destination (where to assign) and
                        [2] source (what to assign)""".trimIndent()
                    )
                }
                if (dest == null) {
                    throw IllegalArgumentException("Destination for assignment cannot be null")
                }
                listOf(
                    MovInstruction(dest, inputs[0])
                )
            }
        )
    }

    private fun createMultiplyPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryMultiplyTreeKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
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
            createEmptyExecPattern(BinaryMultiplyTreeKind)
        )
    }

    private fun createDividePatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryDivideTreeKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
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
            createEmptyExecPattern(BinaryDivideTreeKind)
        )
    }

    private fun createMulDivModInstructions(
        operation: OperationAsm,
        dest: AssignableDest,
        inputs: List<OperandArgumentType>
    ): List<Instruction> {
        // NOTE Needed always, can be either register or memory
        val reg1 = VirtualRegister()
        // NOTE Needed only if second argument is a constant, can be either register or memory
        val reg2 = VirtualRegister()

        return listOf(
            // Save registers modified by assembly instruction
            MovInstruction(dest, PhysicalRegister.RAX),
            MovInstruction(PhysicalRegister.RAX, inputs[0]),
            MovInstruction(reg1, PhysicalRegister.RDX),
        ) + if (inputs[1] is ConstantOperandArgumentType) {
            listOf(
                MovInstruction(reg2, inputs[1]),
                when(operation) {
                    OperationAsm.DIV -> DivInstruction(reg2)
                    OperationAsm.MUL -> MulInstruction(reg2)
                    else -> throw IllegalArgumentException("Bad operation type in createMulDivModInstructions")
                }
            )
        }
        else {
            listOf(
                when(operation) {
                    OperationAsm.DIV -> DivInstruction(reg2)
                    OperationAsm.MUL -> MulInstruction(reg2)
                    else -> throw IllegalArgumentException("Bad operation type in createMulDivModInstructions")
                }
            )
        } + if (dest is Register) {
                listOf(
                // Save result & restore registers
                XchgInstruction(dest, PhysicalRegister.RAX),
                MovInstruction(PhysicalRegister.RDX, reg1)
            )
        }
        else {
            listOf(
                MovInstruction(reg2, dest),
                MovInstruction(dest, PhysicalRegister.RAX),
                MovInstruction(PhysicalRegister.RAX, reg2),
                MovInstruction(PhysicalRegister.RDX, reg1)
            )
        }
    }

    private fun createSafeSimple2ArgPatterns(
        rootOperation: TreeKind,
        instrFactory2arg: (OperandArgumentType, OperandArgumentType) -> Instruction
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

                val src = inputs[0]
                val reg1 = VirtualRegister()
                val firstList = if (dest is Memory && src is Memory) {
                    listOf(
                        MovInstruction(reg1, src),
                        MovInstruction(dest, reg1)
                    )
                } else {
                    listOf(
                        MovInstruction(dest, src)
                    )
                }
                val secondList = create2ArgInstruction(instrFactory2arg, dest, inputs[1])
                firstList + secondList
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun createAndPatterns(): List<InstructionPattern> {
        // NOTE Needed only if at least one argument is a constant, can be either register or memory
        val reg1 = VirtualRegister()

        return createSimpleBoolOperationPatterns(
            BinaryAndTreeKind,
            {par1: OperandArgumentType, par2: OperandArgumentType -> AndInstruction(par1 as AssignableDest, par2)}
        ) + listOf(
            TemplatePattern(BinaryAndTreeKind, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 2) {
                    throw IllegalArgumentException("Boolean and takes exactly two arguments")
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                val shortCutLabel = labelFactory.createLabel("ShortcutAndLabel")
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        MovInstruction(reg1, inputs[0]),
                        CmpInstruction(reg1, NumericalConstant(0)),
                    )
                }
                else {
                    listOf(
                        CmpInstruction(inputs[0] as AssignableDest, NumericalConstant(0))
                    )
                } + listOf(
                    JeInstruction(shortCutLabel)
                ) + if (inputs[1] is ConstantOperandArgumentType) {
                    listOf(
                        MovInstruction(reg1, inputs[1]),
                        CmpInstruction(reg1, NumericalConstant(1)),
                    )
                }
                else {
                    listOf(
                        CmpInstruction(inputs[1] as AssignableDest, NumericalConstant(1))
                    )
                } + listOf(
                    JeInstruction(label),
                    LabelDeclarationInstruction(shortCutLabel)
                )
            }
        )
    }

    private fun createOrPatterns(): List<InstructionPattern> {
        // NOTE Needed only if at least one argument is a constant, can be either register or memory
        val reg1 = VirtualRegister()

        return createSimpleBoolOperationPatterns(
            BinaryOrTreeKind,
            {par1: OperandArgumentType, par2: OperandArgumentType -> OrInstruction(par1 as AssignableDest, par2)}
        ) + listOf(
            TemplatePattern(BinaryOrTreeKind, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 2) {
                    throw IllegalArgumentException("Boolean or takes exactly two arguments")
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        MovInstruction(reg1, inputs[0]),
                        CmpInstruction(reg1, NumericalConstant(0)),
                    )
                }
                else {
                    listOf(
                        CmpInstruction(inputs[0] as AssignableDest, NumericalConstant(0))
                    )
                } + listOf(
                    JneInstruction(label)
                ) + if (inputs[1] is ConstantOperandArgumentType) {
                    listOf(
                        MovInstruction(reg1, inputs[1]),
                        CmpInstruction(reg1, NumericalConstant(0)),
                    )
                }
                else {
                    listOf(
                        CmpInstruction(inputs[1] as AssignableDest, NumericalConstant(0))
                    )
                } + listOf(
                    JneInstruction(label)
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
            XorInstruction(dest, dest),
            // Carry will be set if boolean was not 0
            SubInstruction(dest, boolean),
            // Set dest to 0 once again
            XorInstruction(dest, dest),
            // Add carry to dest + 0
            AdcInstruction(dest, NumericalConstant(0))
            // If carry was set, dest will be equal to 1, otherwise it'll be 0
        )
    }

    private fun createSimpleBoolOperationPatterns(
        rootOperation: TreeKind,
        instrFactory2arg: (OperandArgumentType, OperandArgumentType) -> Instruction
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
                    MovInstruction(dest, reg1)
                ) + convertBooleanTo0Or1(reg1, inputs[1]) +
                        create2ArgInstruction(instrFactory2arg, dest, reg1)
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
                    XorInstruction(reg1, reg1),
                    MovInstruction(dest, NumericalConstant(1))
                ) + create2ArgInstruction(
                    {par1: OperandArgumentType, par2: OperandArgumentType -> CmpInstruction(par1 as AssignableDest, par2)},
                    inputs[0],
                    inputs[1]
                ) + listOf(
                    // NOTE The first operand HAS to be a register (cannot be memory)
                    when(asmCmovOperation) {
                        OperationAsm.CMOVG -> CmovgInstruction(reg1, dest)
                        OperationAsm.CMOVE -> CmoveInstruction(reg1, dest)
                        OperationAsm.CMOVGE -> CmovgeInstruction(reg1, dest)
                        OperationAsm.CMOVL -> CmovlInstruction(reg1, dest)
                        OperationAsm.CMOVLE -> CmovleInstruction(reg1, dest)
                        else -> throw IllegalArgumentException("Bad operation type in createSimpleComparisonPatterns")
                    },
                    MovInstruction(dest, reg1),
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
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        MovInstruction(reg1, inputs[0])
                    ) + create2ArgInstruction(
                        {par1: OperandArgumentType, par2: OperandArgumentType -> CmpInstruction(par1 as AssignableDest, par2)},
                        reg1,
                        inputs[1]
                    )
                } else {
                    create2ArgInstruction(
                        {par1: OperandArgumentType, par2: OperandArgumentType -> CmpInstruction(par1 as AssignableDest, par2)},
                        inputs[0],
                        inputs[1]
                    )
                } + listOf(
                    when(asmJccOperation) {
                        OperationAsm.JG -> JgInstruction(label)
                        OperationAsm.JE -> JeInstruction(label)
                        OperationAsm.JGE -> JgeInstruction(label)
                        OperationAsm.JL -> JlInstruction(label)
                        OperationAsm.JLE -> JleInstruction(label)
                        else -> throw IllegalArgumentException("Bad operation type in createSimpleComparisonPatterns")
                    }
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(rootOperation)
        )
    }

    private fun create2ArgInstruction(
        instrFactory2arg: (OperandArgumentType, OperandArgumentType) -> Instruction,
        operand1: OperandArgumentType,
        operand2: OperandArgumentType
    ): List<Instruction> {
        /* NOTE Needed if:
         *      - both operands are memory, must be a register
         *      - first operand is constant and second is register, can be either register or memory
         *      - first operand is constant and second isn't register, must be a register
         */
        val reg1 = VirtualRegister()

        return if (operand1 is Register) {
            listOf(
                instrFactory2arg(operand1, operand2)
            )
        }
        else if (operand1 is ConstantOperandArgumentType) {
            listOf(
                MovInstruction(reg1, operand2),
                instrFactory2arg(operand1, reg1)
            )
        }
        else if (operand1 is Memory && operand2 is Memory) {
            listOf(
                MovInstruction(reg1, operand2),
                instrFactory2arg(operand1, reg1)
            )
        }
        else {
            listOf(
                instrFactory2arg(operand1, operand2)
            )
        }
    }

    private fun createNotPatterns(): List<InstructionPattern> {
        // NOTE Needed only in JUMP variant if operand is a constant, can be either register or memory
        val reg1 = VirtualRegister()

        return listOf(
            TemplatePattern(UnaryNotTreeKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for value-returning boolean negation cannot be null"
                    )
                }
                if (inputs.size != 1) {
                    throw IllegalArgumentException("Boolean negation takes exactly one argument")
                }
                listOf(
                    MovInstruction(dest, inputs[0]),
                    /* Cannot use single instruction NOT, as it works bitwise:
                     * wouldn't just change 0 -> 1, 1 -> 0, but 0001 -> 1110.
                     * Typical 1 - x also cannot be used directly, as first argument
                     * to SUB cannot be a constant.
                     */
                    SubInstruction(dest, NumericalConstant(1)),
                    NegInstruction(dest)
                )
            },
            TemplatePattern(UnaryNotTreeKind, InstructionKind.JUMP, 1) { _, inputs, label ->
                if (inputs.size != 1) {
                    throw IllegalArgumentException("Boolean negation takes exactly one argument")
                }
                if (label == null) {
                    throw IllegalArgumentException("Label must be passed to jump operation")
                }
                if (inputs[0] is ConstantOperandArgumentType) {
                    listOf(
                        MovInstruction(reg1, inputs[0]),
                        CmpInstruction(reg1, NumericalConstant(0)),
                    )
                }
                else {
                    listOf(
                    CmpInstruction(inputs[0] as AssignableDest, NumericalConstant(0)),
                    )
                } + listOf(
                    JeInstruction(label),
                )
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(UnaryNotTreeKind)
        )
    }

    private fun createNegationPatterns(): List<InstructionPattern> {
        // NOTE Needed only if dest and input are both memory, has to be a register, not memory
        val reg1 = VirtualRegister()

        return listOf(
            TemplatePattern(UnaryMinusTreeKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for negation cannot be null")
                }
                if (inputs.size != 1) {
                    throw IllegalArgumentException("Negation takes exactly one argument")
                }
                val src = inputs[0]
                if (dest is Memory && src is Memory) {
                    listOf(
                        MovInstruction(reg1, src),
                        MovInstruction(dest, reg1),
                        NegInstruction(dest)
                    )
                }
                else {
                    listOf(
                        MovInstruction(dest, inputs[0]),
                        NegInstruction(dest)
                    )
                }
            },
            // NOTE In EXEC version it's equivalent to no-op
            createEmptyExecPattern(UnaryMinusTreeKind)
        )
    }

    private fun createCallPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                CallTreeKind,
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
                    CallInstruction(inputs[0])
                )
            }
        )
    }

    private fun createReturnPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(
                ReturnTreeKind,
                InstructionKind.EXEC,
                1
            ) { _, _, _ ->
                listOf(
                    RetInstruction()
                )
            }
        )
    }

    private fun createPushPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(StackPushTreeKind, InstructionKind.EXEC, 1) { _, inputs, _ ->
                if (inputs.size != 1) {
                    throw IllegalArgumentException(
                        "Push to stack takes exactly one argument"
                    )
                }
                listOf(PushInstruction(inputs[0]))
            }
        )
    }

    private fun createPopPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(StackPopTreeKind, InstructionKind.VALUE, 1) { dest, _, _ ->
                if (dest == null) {
                    throw IllegalArgumentException("Destination for pop from stack should not be null")
                }
                listOf(PopInstruction(dest))
            }
        )
    }

    private fun createMemoryReadPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(MemoryTreeKind, InstructionKind.VALUE, 1) { dest, inputs, _ ->
                if (dest == null) {
                    throw IllegalArgumentException(
                        "Destination for read from memory should not be null"
                    )
                }
                if (inputs.size != 1) {
                    throw IllegalArgumentException(
                        "Read from memory takes exactly one argument"
                    )
                }
                listOf(MovInstruction(dest, inputs[0]))
            }
        )
    }

    private fun createEmptyExecPattern(rootType: TreeKind): TemplatePattern {
        return TemplatePattern(rootType, InstructionKind.EXEC, 1) { _, _ , _-> listOf() }
    }

    private class LabelDeclarationInstruction(private val label: Label) : Instruction {

        override fun toAsm(mapping: Map<Register, PhysicalRegister>): String {
            return label.name + ":"
        }
    
        override fun usedRegisters(): List<Register> {
            return listOf()
        }
    
        override fun definedRegisters(): List<Register> {
            return listOf()
        }
    
        override fun isCopy(): Boolean {
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
