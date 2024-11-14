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
            BinaryOperationType.ADD to createAddPatterns(),
        )
    }

    private fun createAddPatterns(): List<InstructionPattern> {
        return listOf(
            TemplatePattern(BinaryOperationType.ADD, InstructionKind.VALUE, 1) { operands, destRegister ->
                when {
                    // Case: Register + Register
                    operands[0] is Register && operands[1] is Register -> listOf(
                        Instruction(OperationAsm.MOV, listOf(operands[0], PhysicalRegister(0))), //TODO: Change PhysicalRegister to enum??
                        Instruction(OperationAsm.ADD, listOf(destRegister, operands[1]))
                    )
                    // Case: Register + Memory
                    operands[0] is Register && operands[1] is Memory -> listOf(
                        Instruction(OperationAsm.MOV, listOf(operands[1], PhysicalRegister(0))),
                        Instruction(OperationAsm.ADD, listOf(destRegister, operands[0]))
                    )
                    else -> throw IllegalArgumentException("Unsupported operand types for ADD pattern")
                }
            }
        )
    }
}
