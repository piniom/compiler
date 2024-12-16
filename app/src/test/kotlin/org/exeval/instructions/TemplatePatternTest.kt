package org.exeval.instructions

import org.exeval.cfg.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TemplatePatternTest {

	class TestInstruction(val arguments: List<OperandArgumentType>): Instruction {
		override fun toAsm(mapping: Map<Register, PhysicalRegister>): String = ""
		override fun usedRegisters(): List<Register> = listOf()
		override fun definedRegisters(): List<Register> = listOf()
		override fun isCopy(): Boolean = false
	}

	private fun createPattern() =
		TemplatePattern(
			BinaryAddTreeKind,
			InstructionKind.EXEC,
			1,
			{_, inputs, _ -> listOf(TestInstruction(inputs))}
		)

	private fun createTree(left: Tree, right: Tree) =
		BinaryOperationTree(left, right, BinaryTreeOperationType.ADD)

	fun validateInstructionCreation(
		matchResult: InstructionMatchResult,
		inputs: List<VirtualRegister>
	): TestInstruction {
		val instructions = matchResult.createInstruction(null, inputs, null)
		assertEquals(1, instructions.size, "Exactly one instruction should be returned")
		assertTrue(
			instructions[0] is TestInstruction,
			"Returned instruction should be a TestInstruction"
		)

		val instr = instructions[0] as TestInstruction
		assertEquals(
			2,
			instr.arguments.size,
			"Exactly two arguments should be passed to instantiated instruction"
		)
		return instr
	}

	@Test
	fun `simple match only virtual registers`() {
		val pattern = createPattern()
		val reg1 = VirtualRegister()
		val reg2 = VirtualRegister()
		val tree = createTree(RegisterTree(reg1), RegisterTree(reg2))

		val matchResult = pattern.matches(tree)
		assertNotNull(matchResult, "Match should succeed")

		val instr = validateInstructionCreation(matchResult!!, listOf(reg1, reg2))
		assertSame(reg1, instr.arguments[0])
		assertSame(reg2, instr.arguments[1])
	}

	@Test
	fun `simple match only physical registers`() {
		val pattern = createPattern()
		val tree = createTree(RegisterTree(PhysicalRegister.RAX), RegisterTree(PhysicalRegister.R13))

		val matchResult = pattern.matches(tree)
		assertNotNull(matchResult, "Match should succeed")

		val instr = validateInstructionCreation(matchResult!!, emptyList<VirtualRegister>())
		assertSame(PhysicalRegister.RAX, instr.arguments[0])
		assertSame(PhysicalRegister.R13, instr.arguments[1])
	}

	@Test
	fun `simple match with constant`() {
		val pattern = createPattern()
		val reg = VirtualRegister()
		val tree = createTree(NumericalConstantTree(3), RegisterTree(reg))

		val matchResult = pattern.matches(tree)
		assertNotNull(matchResult, "Match should succeed")

		val instr = validateInstructionCreation(matchResult!!, listOf(reg))
		assertTrue(instr.arguments[0] is NumericalConstant)
		assertEquals(3, (instr.arguments[0] as NumericalConstant).value)
		assertSame(reg, instr.arguments[1])
	}

	@Test
	fun `simple match with delayed constant`() {
		val pattern = createPattern()
		val reg = VirtualRegister()
		val tree = createTree(RegisterTree(reg), DelayedNumericalConstantTree({ 4 }))

		val matchResult = pattern.matches(tree)
		assertNotNull(matchResult, "Match should succeed")

		val instr = validateInstructionCreation(matchResult!!, listOf(reg))
		assertSame(reg, instr.arguments[0])
		assertTrue(instr.arguments[1] is DelayedNumericalConstant)
		assertEquals(4, (instr.arguments[1] as DelayedNumericalConstant).getValue())
	}

	@Test
	fun `simple match with label`() {
		val pattern = createPattern()
		val reg = VirtualRegister()
		val label = Label("label")
		val tree = createTree(RegisterTree(reg), LabelConstantTree(label))

		val matchResult = pattern.matches(tree)
		assertNotNull(matchResult, "Match should succeed")

		val instr = validateInstructionCreation(matchResult!!, listOf(reg))
		assertSame(reg, instr.arguments[0])
		assertSame(label, instr.arguments[1])
	}

}
