package org.exeval.instructions

import org.exeval.cfg.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InstructionCovererTest {
    private fun genericTest(coverer: InstructionCoverer, inputTreesToExpected: Map<Tree, List<Instruction>>) {
        for ((input, expected) in inputTreesToExpected) {
            val actual = coverer.cover(input)
            assertEquals(expected = expected, actual = actual, message = "Error on input $input\n")
        }
    }

    @Test
    fun `Simple assignments work`() {
        val instructionSet: Map<OperationType, List<InstructionPattern>> = mapOf(
            BinaryOperationType.ASSIGNMENT to listOf(
                TemplatePattern(
                    BinaryOperationType.ASSIGNMENT, InstructionKind.VALUE, cost = 1
                ) { operands, destRegister ->
                    if (destRegister == null) {
                        throw IllegalArgumentException("Destination register for assignment cannot be null")
                    }
                    if (destRegister is Memory && operands[0] is Memory) {
                        throw IllegalArgumentException("Both operands are memory")
                    } else {
                        listOf(
                            Instruction(OperationAsm.MOV, listOf(destRegister, operands[0]))
                        )
                    }
                }
            ),
        )

        val assignmentCoverer = InstructionCoverer(instructionSet)

        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(0), VirtualRegister(5), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(5))),
            ),

            BinaryOperation(
                VirtualRegister(17), VirtualRegister(17), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(17), VirtualRegister(17))),
            ),

            BinaryOperation(
                VirtualRegister(17), Memory(Constant(1032)), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(17), Memory(Constant(1032)))),
            ),

            BinaryOperation(
                Memory(Constant(1032)), VirtualRegister(17), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(Memory(Constant(1032)), VirtualRegister(17))),
            ),

            BinaryOperation(
                VirtualRegister(17), Memory(VirtualRegister(5)), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(17), Memory(VirtualRegister(5)))),
            ),

            BinaryOperation(
                Memory(VirtualRegister(5)), VirtualRegister(17), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(Memory(VirtualRegister(5)), VirtualRegister(17))),
            ),
        )

        genericTest(assignmentCoverer, inputTreesToExpected)
    }
}