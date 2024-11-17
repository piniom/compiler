package org.exeval.instructions

import org.exeval.cfg.*

class InstructionSetCreator {
    private val patterns: Map<OperationType, List<InstructionPattern>>

    init {
        patterns = initInstructionSet()
    }

    fun createInstructionSet(): Map<OperationType, List<InstructionPattern>> = patterns


    // Private

    private fun initInstructionSet(): Map<OperationType, List<InstructionPattern>> {
        return mapOf(
            // TODO assignment may need one additional register
            BinaryOperationType.ASSIGNMENT to createAssignmentPatterns(),
            BinaryOperationType.ADD to createSafeSimple2ArgPattern(BinaryOperationType.ADD, OperationAsm.ADD),
            BinaryOperationType.SUBTRACT to createSafeSimple2ArgPattern(BinaryOperationType.SUBTRACT, OperationAsm.SUB),
            // TODO multiply, divide, and modulo need at least one, up to two additional registers
            BinaryOperationType.MULTIPLY to createMultiplyPatterns(),
            BinaryOperationType.DIVIDE to createDividePatterns(),
            BinaryOperationType.MODULO to createModuloPatterns(),

            /* TODO "and", "or", "xor", "not" will work correctly only with values 0 and 1
             *      Where in code should non-zero integers be converted to 1?
             */
            BinaryOperationType.AND to createSafeSimple2ArgPattern(BinaryOperationType.AND, OperationAsm.AND),
            BinaryOperationType.OR to createSafeSimple2ArgPattern(BinaryOperationType.OR, OperationAsm.OR),
            BinaryOperationType.XOR to createSafeSimple2ArgPattern(BinaryOperationType.XOR, OperationAsm.XOR),

            /* TODO these three need to be able to create new labels
            BinaryOperationType.GREATER
            BinaryOperationType.GREATER_EQUAL
            BinaryOperationType.EQUAL
            */

            UnaryOperationType.NOT to createNotPatterns(),
            UnaryOperationType.MINUS to createNegationPatterns(),
            UnaryOperationType.INCREMENT to createIncrementPatterns(),
            UnaryOperationType.DECREMENT to createDecrementPatterns(),

            // TODO Whose responsibility is to prepare function arguments?
            // TODO Call is in fact a unary operation, taking as argument address or label of destination function
            NullaryOperationType.CALL to createCallPatterns(),
            NullaryOperationType.RETURN to createReturnPatterns(),
        )
    }

    private fun createAssignmentPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.ASSIGNMENT, InstructionKind.VALUE, 1) { operands, destRegister ->
                if (destRegister is Memory && operands[0] is Memory) {
                    // TODO additional register is needed
                    // Proposition:
                    // listOf(
                    //     Instruction(OperationAsm.MOV, listOf(VirtualRegister(WorkingRegisters.R0), operands[0])),
                    //     Instruction(OperationAsm.MOV, listOf(destRegister, VirtualRegister(WorkingRegisters.R0)))
                    // )
                    throw IllegalArgumentException("Unsupported operand types for MOV")
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
                    Instruction(OperationAsm.MOV, listOf(destRegister, PhysicalRegister(3 /* TODO does 3 correspond to rdx? */ ))),
                )
            }
        )
    }

    private fun createMulDivModInstructions(operation: OperationAsm, operands: List<OperandArgumentType>, destRegister: Assignable): List<Instruction> {
        return listOf(
            // Proposition - use eg. PhysicalRegister(Registers.RAX)
            Instruction(OperationAsm.MOV, listOf(destRegister, PhysicalRegister(0))), //TODO: Change PhysicalRegister to enum??
            Instruction(OperationAsm.MOV, listOf(PhysicalRegister(0), operands[0])),
            // TODO rdx can also be changed by mul and div, one more register is needed to save it
            // Proposition - use eg. VirtualRegister(WorkingRegisters.R0) etc.
            when (operands[1]) {
                // Case: Register or Memory
                is Assignable -> Instruction(operation, listOf(operands[1]))
                // Case: Constant
                is Constant -> /* TODO register is needed to pass the value */ throw IllegalArgumentException("Unsupported operand types for MUL/DIV pattern")
            },
            Instruction(OperationAsm.XCHG, listOf(destRegister, PhysicalRegister(0))),
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
                //TODO may happen with cmp, or when destRegister is mapped to memory, and not a physical register

                throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
            )
            // Case: Register + Constant
            operand1 is Register && operand2 is Constant -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Register
            operand1 is Constant && operand2 is Register -> listOf(
                //TODO may happen with cmp

                throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
            )
            // Case: Memory + Constant
            operand1 is Memory && operand2 is Constant -> listOf(
                Instruction(operation, listOf(operand1, operand2))
            )
            // Case: Constant + Memory
            operand1 is Constant && operand2 is Memory -> listOf(
                //TODO may happen with cmp

                throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
            )
            // Case: Constant + Constant
            operand1 is Constant && operand2 is Constant -> listOf(
                //TODO may happen with cmp

                throw IllegalArgumentException("Unsupported operand types for 2-argument instuction ${operation}")
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
            // TODO call is in fact unary, and not nullary
            TemplatePattern(NullaryOperationType.CALL, InstructionKind.VALUE, 1) { operands, destRegister ->
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
