package org.exeval.instructions

import org.exeval.cfg.Label
import org.exeval.instructions.interfaces.LivenessChecker
import org.exeval.instructions.linearizer.BasicBlock
import org.junit.Test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.exeval.cfg.Register
import kotlin.test.assertEquals

class LivenessCheckerTest {

    @Test
    fun simpleTest(){
        val checker: LivenessChecker = LivenessCheckerImpl()
        val r1 = mockk<Register>()
        val r2 = mockk<Register>()
        val r3 = mockk<Register>()

        val i1 = mockk<Instruction>()
        every { i1.definedRegisters() } returns listOf(r1)
        every { i1.usedRegisters() } returns listOf()
        every { i1.isCopy() } returns false

        val i2 = mockk<Instruction>()
        every { i2.definedRegisters() } returns listOf(r2)
        every { i2.usedRegisters() } returns listOf()
        every { i2.isCopy() } returns false

        val i3 = mockk<Instruction>()
        every { i3.definedRegisters() } returns listOf(r1)
        every { i3.usedRegisters() } returns listOf(r2)
        every { i3.isCopy() } returns false

        val i4 = mockk<Instruction>()
        every { i4.definedRegisters() } returns listOf(r3)
        every { i4.usedRegisters() } returns listOf(r2)
        every { i4.isCopy() } returns false
        // r1 = 0
        // r2 = 0
        // r1 = r2 + 1
        // r3 = r2 + 1
        val bb3 = BasicBlock(
            label = Label("label3"),
            instructions = listOf(i4),
            successors = listOf()
        )
        val bb2 = BasicBlock(
            label = Label("label2"),
            instructions = listOf(i3),
            successors = listOf(bb3)
        )
        val bb1 = BasicBlock(
            label = Label("label1"),
            instructions = listOf(
                i1, i2
            ),
            successors = listOf(bb2)
        )

        val input: List<BasicBlock> = listOf(
            bb1,
            bb2,
            bb3
        )
        val result = checker.check(input)
        val expectedInterference = mapOf(
            Pair(r1, setOf(r2)),
            Pair(r2, setOf(r1)),
            Pair(r3, setOf())
        )
        val expectedCopy = mapOf<Register, Set<Register>>(
            Pair(r1, setOf()),
            Pair(r2, setOf()),
            Pair(r3, setOf())
        )

        assertEquals(result.interference, expectedInterference, result.interference.toString() + " " + expectedInterference.toString() )
        assertEquals(result.copy, expectedCopy, result.copy.toString() + " " + expectedCopy.toString())
    }
    @Test
    fun simpleCopyTest(){
        val checker: LivenessChecker = LivenessCheckerImpl()
        val r1 = mockk<Register>()
        val r2 = mockk<Register>()
        val r3 = mockk<Register>()

        val i1 = mockk<Instruction>()
        every { i1.definedRegisters() } returns listOf(r1)
        every { i1.usedRegisters() } returns listOf()
        every { i1.isCopy() } returns false

        val i2 = mockk<Instruction>()
        every { i2.definedRegisters() } returns listOf(r2)
        every { i2.usedRegisters() } returns listOf()
        every { i2.isCopy() } returns false

        val i3 = mockk<Instruction>()
        every { i3.definedRegisters() } returns listOf(r1)
        every { i3.usedRegisters() } returns listOf(r2)
        every { i3.isCopy() } returns true

        val i4 = mockk<Instruction>()
        every { i4.definedRegisters() } returns listOf(r3)
        every { i4.usedRegisters() } returns listOf(r2)
        every { i4.isCopy() } returns false
        // r1 = 0
        // r2 = 1
        // r1 = r2
        // r3 = r2 + 1
        val bb3 = BasicBlock(
            label = Label("label3"),
            instructions = listOf(i4),
            successors = listOf()
        )
        val bb2 = BasicBlock(
            label = Label("label2"),
            instructions = listOf(i3),
            successors = listOf(bb3)
        )
        val bb1 = BasicBlock(
            label = Label("label1"),
            instructions = listOf(
                i1, i2
            ),
            successors = listOf(bb2)
        )

        val input: List<BasicBlock> = listOf(
            bb1,
            bb2,
            bb3
        )
        val result = checker.check(input)
        val expectedInterference = mapOf<Register, Set<Register>>(
            Pair(r1, setOf()),
            Pair(r2, setOf()),
            Pair(r3, setOf())
        )
        val expectedCopy = mapOf<Register, Set<Register>>(
            Pair(r1, setOf(r2)),
            Pair(r2, setOf(r1)),
            Pair(r3, setOf())
        )

        assertEquals(result.interference, expectedInterference, result.interference.toString() + " " + expectedInterference.toString() )
        assertEquals(result.copy, expectedCopy, result.copy.toString() + " " + expectedCopy.toString())
    }
}