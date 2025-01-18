package org.exeval.cfg

import org.exeval.cfg.interfaces.UsableMemoryCell
import kotlin.test.Test
import kotlin.test.assertEquals

class VarAccessGeneratorTest {
	@Test
	fun `Generate accesses for RBP`() {
		val generator = VarAccessGenerator(RegisterTree(PhysicalRegister.RBP))

		val register = VirtualRegister()
		val regCell = UsableMemoryCell.VirtReg(register)
		val memCell = UsableMemoryCell.MemoryPlace(24)

		val regExpected = RegisterTree(register)
		val memExpected =
			MemoryTree(
				BinaryOperationTree(
					RegisterTree(PhysicalRegister.RBP),
					NumericalConstantTree(24),
					BinaryTreeOperationType.SUBTRACT,
				),
			)

		val regActual = generator.generateVarAccess(regCell)
		val memActual = generator.generateVarAccess(memCell)

		assertEquals(regExpected, regActual)
		assertEquals(memExpected, memActual)
	}

	@Test
	fun `Generate accesses for offsets`() {
		val offset: Long = 1028
		val generator = VarAccessGenerator(NumericalConstantTree(offset))

		val register = VirtualRegister()
		val regCell = UsableMemoryCell.VirtReg(register)
		val memCell = UsableMemoryCell.MemoryPlace(24)

		val regExpected = RegisterTree(register)
		val memExpected =
			MemoryTree(
				BinaryOperationTree(
					NumericalConstantTree(offset),
					NumericalConstantTree(24),
					BinaryTreeOperationType.SUBTRACT,
				),
			)

		val regActual = generator.generateVarAccess(regCell)
		val memActual = generator.generateVarAccess(memCell)

		assertEquals(regExpected, regActual)
		assertEquals(memExpected, memActual)
	}
}
