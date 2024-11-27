package org.exeval.cfg

import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.cfg.constants.Registers
import kotlin.test.Test
import kotlin.test.assertEquals

class VarAccessGeneratorTest {
    @Test
    fun `Generate accesses for RBP`() {
        val generator = VarAccessGenerator(PhysicalRegister(Registers.RBP))

        val regCell = UsableMemoryCell.VirtReg(108)
        val memCell = UsableMemoryCell.MemoryPlace(24)

        val regExpected = VirtualRegister(108)
        val memExpected = Memory(
            BinaryOperation(
                PhysicalRegister(Registers.RBP),
                Constant(24),
                BinaryOperationType.SUBTRACT
            )
        )

        val regActual = generator.generateVarAccess(regCell)
        val memActual = generator.generateVarAccess(memCell)

        assertEquals(regExpected, regActual)
        assertEquals(memExpected, memActual)
    }

    @Test
    fun `Generate accesses for offsets`() {
        val offset = 1028
        val generator = VarAccessGenerator(Constant(offset))

        val regCell = UsableMemoryCell.VirtReg(108)
        val memCell = UsableMemoryCell.MemoryPlace(24)

        val regExpected = VirtualRegister(108)
        val memExpected = Memory(
            BinaryOperation(
                Constant(offset),
                Constant(24),
                BinaryOperationType.SUBTRACT
            )
        )

        val regActual = generator.generateVarAccess(regCell)
        val memActual = generator.generateVarAccess(memCell)

        assertEquals(regExpected, regActual)
        assertEquals(memExpected, memActual)
    }
}