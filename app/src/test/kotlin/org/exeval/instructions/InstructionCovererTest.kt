package org.exeval.instructions

import org.exeval.cfg.BinaryOperation
import org.exeval.cfg.BinaryOperationType
import org.exeval.cfg.Constant
import org.exeval.cfg.Memory
import org.exeval.cfg.VirtualRegister
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InstructionCovererTest {
    private val coverer: InstructionCoverer

    init {
        val instructionSetCreator = InstructionSetCreator()
        val allPatterns = instructionSetCreator.createInstructionSet().values.flatten()
        coverer = InstructionCoverer(allPatterns.toSet())
    }

    @Test
    fun `Simple assignments work`() {
        val inputTrees = listOf(
            BinaryOperation(
                VirtualRegister(0),
                VirtualRegister(5),
                BinaryOperationType.ASSIGNMENT
            ),
            BinaryOperation(
                VirtualRegister(17),
                VirtualRegister(17),
                BinaryOperationType.ASSIGNMENT
            ),
            BinaryOperation(
                VirtualRegister(17),
                Memory(Constant(1032)),
                BinaryOperationType.ASSIGNMENT
            ),
            BinaryOperation(
                Memory(Constant(1032)),
                VirtualRegister(17),
                BinaryOperationType.ASSIGNMENT
            ),
            BinaryOperation(
                VirtualRegister(17),
                Memory(VirtualRegister(5)),
                BinaryOperationType.ASSIGNMENT
            ),
            BinaryOperation(
                Memory(VirtualRegister(5)),
                VirtualRegister(17),
                BinaryOperationType.ASSIGNMENT
            ),
        )
        val expected = listOf(
            listOf(
                Instruction(
                    OperationAsm.MOV, listOf(
                        VirtualRegister(0),
                        VirtualRegister(5)
                    )
                ),
            ),
            listOf(
                Instruction(
                    OperationAsm.MOV, listOf(
                        VirtualRegister(17),
                        VirtualRegister(17)
                    )
                ),
            ),
            listOf(
                Instruction(
                    OperationAsm.MOV, listOf(
                        VirtualRegister(17),
                        Memory(Constant(1032)),
                    )
                ),
            ),
            listOf(
                Instruction(
                    OperationAsm.MOV, listOf(
                        Memory(Constant(1032)),
                        VirtualRegister(17),
                    )
                ),
            ),
            listOf(
                Instruction(
                    OperationAsm.MOV, listOf(
                        VirtualRegister(17),
                        Memory(VirtualRegister(5)),
                    )
                ),
            ),
            listOf(
                Instruction(
                    OperationAsm.MOV, listOf(
                        Memory(VirtualRegister(5)),
                        VirtualRegister(17),
                    )
                ),
            ),
        )

        val actual = inputTrees.map { coverer.cover(it) }
        assertEquals(expected, actual)
    }

    @Test
    fun `Simple comparisons work`() {

    }

    @Test
    fun `Int BinaryOperations work`() {

    }

    @Test
    fun `Int UnaryOps work`() {

    }

    @Test
    fun `Boolean unary Not operation works`() {

    }

    @Test
    fun `Operations inside memory work`() {

    }

    @Test
    fun `Boolean operations work`() {

    }

    @Test
    fun `Call and Return work`() {

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
//    fun `Always true, false expressions are represented as true, false`(){
//
//    }
}