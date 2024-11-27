package org.exeval.cfg

import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.cfg.constants.Registers
import kotlin.test.Test
import kotlin.test.assertEquals

class VarAccessGeneratorTest {
    @Test
    fun `Generate accesses for RBP`() {
        val generator = VarAccessGenerator()

        val regCell = UsableMemoryCell.VirtReg(108)
        val memCell = UsableMemoryCell.MemoryPlace(24)

        val regExpected = VirtualRegisterTree(108)
        val memExpected = MemoryTree(
            BinaryOperationTree(
                PhysicalRegisterTree(Registers.RBP),
                ConstantTree(24),
                BinaryTreeOperationType.SUBTRACT
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
        val generator = VarAccessGenerator(offset)

        val regCell = UsableMemoryCell.VirtReg(108)
        val memCell = UsableMemoryCell.MemoryPlace(24)

        val regExpected = VirtualRegisterTree(108)
        val memExpected = MemoryTree(
            BinaryOperationTree(
                ConstantTree(offset),
                ConstantTree(24),
                BinaryTreeOperationType.SUBTRACT
            )
        )

        val regActual = generator.generateVarAccess(regCell)
        val memActual = generator.generateVarAccess(memCell)

        assertEquals(regExpected, regActual)
        assertEquals(memExpected, memActual)
    }
}