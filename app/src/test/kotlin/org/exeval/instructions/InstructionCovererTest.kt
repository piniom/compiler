package org.exeval.instructions

import org.exeval.cfg.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InstructionCovererTest {
    private val coverer: InstructionCoverer

    init {
        val instructionSetCreator = InstructionSetCreator()
        val allPatterns = instructionSetCreator.createInstructionSet().values.flatten()
        coverer = InstructionCoverer(allPatterns.toSet())
    }

    private fun genericTest(inputTreesToExpected: Map<Tree, List<Instruction>>) {
        for ((input, expected) in inputTreesToExpected) {
            val actual = coverer.cover(input)
            assertEquals(expected = expected, actual = actual, message = "Error on input $input\n")
        }
    }

    @Test
    fun `Simple assignments work`() {
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

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Simple comparisons work`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(0), VirtualRegister(5), BinaryOperationType.GREATER
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(VirtualRegister(5), VirtualRegister(0))),
            ),

            BinaryOperation(
                VirtualRegister(0), VirtualRegister(5), BinaryOperationType.GREATER_EQUAL
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(VirtualRegister(5), VirtualRegister(0))),
            ),

            BinaryOperation(
                VirtualRegister(0), VirtualRegister(5), BinaryOperationType.EQUAL
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(VirtualRegister(5), VirtualRegister(0))),
            ),

            BinaryOperation(
                VirtualRegister(17), VirtualRegister(17), BinaryOperationType.EQUAL
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(VirtualRegister(17), VirtualRegister(17))),
            ),

            BinaryOperation(
                VirtualRegister(17), Memory(Constant(1032)), BinaryOperationType.GREATER
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(Memory(Constant(1032)), VirtualRegister(17))),
            ),

            BinaryOperation(
                Memory(Constant(1032)), VirtualRegister(17), BinaryOperationType.GREATER
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(17), Memory(Constant(1032)))),
            ),

            BinaryOperation(
                VirtualRegister(17), Memory(VirtualRegister(5)), BinaryOperationType.GREATER
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(Memory(VirtualRegister(5)), VirtualRegister(17))),
            ),

            BinaryOperation(
                Memory(VirtualRegister(5)), VirtualRegister(17), BinaryOperationType.GREATER
            ) to listOf(
                Instruction(OperationAsm.CMP, listOf(VirtualRegister(17), Memory(VirtualRegister(5)))),
            ),
        )

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Add and Sub for ints work`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), Constant(10), BinaryOperationType.ADD
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(1))),
                Instruction(OperationAsm.ADD, listOf(VirtualRegister(2), Constant(10))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(2))),
            ),

            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), Constant(10), BinaryOperationType.SUBTRACT
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(1))),
                Instruction(OperationAsm.SUB, listOf(VirtualRegister(2), Constant(10))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(2))),
            ),
        )

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Mul, Div and Mod for ints work`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), VirtualRegister(2), BinaryOperationType.MULTIPLY
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(3), PhysicalRegister(0))),
                Instruction(OperationAsm.MOV, listOf(PhysicalRegister(0), VirtualRegister(1))),
                Instruction(OperationAsm.MUL, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.XCHG, listOf(VirtualRegister(3), PhysicalRegister(0))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(3)))
            ),

            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), VirtualRegister(2), BinaryOperationType.DIVIDE
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(3), PhysicalRegister(0))),
                Instruction(OperationAsm.MOV, listOf(PhysicalRegister(0), VirtualRegister(1))),
                Instruction(OperationAsm.DIV, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.XCHG, listOf(VirtualRegister(3), PhysicalRegister(0))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(3)))
            ),

            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), VirtualRegister(2), BinaryOperationType.MODULO
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(3), PhysicalRegister(0))),
                Instruction(OperationAsm.MOV, listOf(PhysicalRegister(0), VirtualRegister(1))),
                Instruction(OperationAsm.DIV, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.XCHG, listOf(VirtualRegister(3), PhysicalRegister(0))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(3), PhysicalRegister(3))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(3)))
            ),
        )

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Boolean binary operations work`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), Constant(10), BinaryOperationType.AND
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(1))),
                Instruction(OperationAsm.AND, listOf(VirtualRegister(2), Constant(10))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(2))),
            ),

            BinaryOperation(
                VirtualRegister(0), BinaryOperation(
                    VirtualRegister(1), Constant(10), BinaryOperationType.OR
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(1))),
                Instruction(OperationAsm.OR, listOf(VirtualRegister(2), Constant(10))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(2))),
            ),
        )

        BinaryOperation(
            VirtualRegister(0), BinaryOperation(
                VirtualRegister(1), Constant(10), BinaryOperationType.XOR
            ), BinaryOperationType.ASSIGNMENT
        ) to listOf(
            Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(1))),
            Instruction(OperationAsm.XOR, listOf(VirtualRegister(2), Constant(10))),
            Instruction(OperationAsm.MOV, listOf(VirtualRegister(0), VirtualRegister(2))),
        )

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Int UnaryOps work`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(1), UnaryOp(
                    VirtualRegister(0), UnaryOperationType.MINUS
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(0))),
                Instruction(OperationAsm.NEG, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(1), VirtualRegister(2)))
            ),
            BinaryOperation(
                VirtualRegister(1), UnaryOp(
                    VirtualRegister(0), UnaryOperationType.INCREMENT
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(0))),
                Instruction(OperationAsm.INC, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(1), VirtualRegister(2)))
            ),
            BinaryOperation(
                VirtualRegister(1), UnaryOp(
                    VirtualRegister(0), UnaryOperationType.DECREMENT
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(0))),
                Instruction(OperationAsm.DEC, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(1), VirtualRegister(2)))
            ),
        )

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Boolean unary Not operation works`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            BinaryOperation(
                VirtualRegister(1), UnaryOp(
                    VirtualRegister(0), UnaryOperationType.NOT
                ), BinaryOperationType.ASSIGNMENT
            ) to listOf(
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(2), VirtualRegister(0))),
                Instruction(OperationAsm.SUB, listOf(VirtualRegister(2), Constant(1))),
                Instruction(OperationAsm.NEG, listOf(VirtualRegister(2))),
                Instruction(OperationAsm.MOV, listOf(VirtualRegister(1), VirtualRegister(2)))
            ),
        )

        genericTest(inputTreesToExpected)
    }

    @Test
    fun `Operations inside memory work`() {
        val input = BinaryOperation(
            Memory(
                BinaryOperation(
                    BinaryOperation(
                        VirtualRegister(0), VirtualRegister(1), BinaryOperationType.XOR
                    ), BinaryOperation(
                        VirtualRegister(2), VirtualRegister(3), BinaryOperationType.MULTIPLY
                    ), BinaryOperationType.SUBTRACT
                )
            ), Constant(42), BinaryOperationType.ASSIGNMENT
        )

        val mulInstructions = listOf(
            Instruction(OperationAsm.MOV, listOf(VirtualRegister(4), PhysicalRegister(0))),
            Instruction(OperationAsm.MOV, listOf(PhysicalRegister(0), VirtualRegister(2))),
            Instruction(OperationAsm.MUL, listOf(VirtualRegister(3))),
            Instruction(OperationAsm.XCHG, listOf(VirtualRegister(4), PhysicalRegister(0))),
        )
        val xorInstructions = listOf(
            Instruction(OperationAsm.MOV, listOf(VirtualRegister(5), VirtualRegister(0))),
            Instruction(OperationAsm.XOR, listOf(VirtualRegister(5), VirtualRegister(1))),
        )
        val subInstructions = listOf(
            Instruction(OperationAsm.MOV, listOf(VirtualRegister(6), VirtualRegister(5))),
            Instruction(OperationAsm.SUB, listOf(VirtualRegister(6), VirtualRegister(4))),
        )
        val assInstructions = listOf(
            Instruction(OperationAsm.MOV, listOf(Memory(VirtualRegister(6)), Constant(42)))
        )
        val expected = mulInstructions + xorInstructions + subInstructions + assInstructions

        val actual = coverer.cover(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Return work`() {
        val inputTreesToExpected: Map<Tree, List<Instruction>> = mapOf(
            Return to listOf(
                Instruction(OperationAsm.RET, emptyList())
            )
        )

        genericTest(inputTreesToExpected)
    }

//    @Disabled("No lea instruction jet")
//    @Test
//    fun `lea optimization`() {
//
//    }
//
//    @Disabled("We do not have such optimization jet")
//    @Test
//    fun `Nested unary minus gets flattened`() {
//
//    }
//    @Disabled("No true Tree")
//    @Test
//    fun `true and false expressions are always represented as true and false`(){
//
//    }
}