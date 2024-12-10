package org.exeval.instructions

import com.sun.source.tree.BinaryTree
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.exeval.cfg.*
import org.exeval.instructions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class InstructionCovererTest {

    @Test
    fun `cover should return instructions based on patterns`() {
        val left = NumericalConstantTree(1)
        val right = NumericalConstantTree(2)
        // Arrange
        val tree = BinaryOperationTree(left, right, BinaryTreeOperationType.ADD)
        val mockPattern = mockk<InstructionPattern>()
        val mockInstruction = mockk<Instruction>()
        val mockInstruction2 = mockk<Instruction>();


        val instructionPatterns = mapOf(
            InstructionPatternMapKey(tree.treeKind(), InstructionKind.EXEC) to listOf(mockPattern),
            InstructionPatternMapKey(left.treeKind(), InstructionKind.VALUE) to listOf(mockPattern),
            InstructionPatternMapKey(right.treeKind(), InstructionKind.EXEC) to listOf(mockPattern)
        )
        val instructionKind = InstructionKind.VALUE
        val instructionCoverer = InstructionCoverer(instructionPatterns)


        val matchResult = InstructionMatchResult(
            children = listOf(left, right),
            createInstruction = { _, _, _ -> listOf(mockInstruction) }
        )

        val anyMatchResult = InstructionMatchResult(
            children = listOf(),
            createInstruction = { _, _, _ -> listOf(mockInstruction2) }
        )

        every { mockPattern.matches(any()) } returns anyMatchResult
        every { mockPattern.matches(tree) } returns matchResult
        every { mockPattern.kind } returns instructionKind
        every { mockPattern.cost } returns 1

        val resultInstructions = instructionCoverer.cover(tree, null)

        assertEquals(
            listOf(mockInstruction2, mockInstruction2, mockInstruction),
            resultInstructions
        )
    }

    @Test
    fun `cover should return cheapest cover (simple)`() {
        val left = NumericalConstantTree(1)
        val right = NumericalConstantTree(2)
        val tree = BinaryOperationTree(left, right, BinaryTreeOperationType.ADD)
        val expensiveMockPattern = mockk<InstructionPattern>()
        val mockPattern = mockk<InstructionPattern>()
        val expensiveMockInstruction = mockk<Instruction>()
        val mockInstruction = mockk<Instruction>()

        val instructionPatterns = mapOf(
            InstructionPatternMapKey(tree.treeKind(), InstructionKind.EXEC) to listOf(mockPattern),
            InstructionPatternMapKey(left.treeKind(), InstructionKind.VALUE) to listOf(mockPattern),
            InstructionPatternMapKey(right.treeKind(), InstructionKind.EXEC) to listOf(mockPattern)
        )
        val instructionKind = InstructionKind.VALUE
        val instructionCoverer = InstructionCoverer(instructionPatterns)


        val matchResult = InstructionMatchResult(
            children = listOf(),
            createInstruction = { _, _, _ -> listOf(mockInstruction) }
        )

        val expensiveMatchResult = InstructionMatchResult(
            children = listOf(),
            createInstruction = { _, _, _ -> listOf(expensiveMockInstruction) }
        )

        every { expensiveMockPattern.matches(any()) } returns null
        every { expensiveMockPattern.matches(tree) } returns expensiveMatchResult
        every { expensiveMockPattern.kind } returns instructionKind
        every { expensiveMockPattern.cost } returns 1000

        every { mockPattern.matches(any()) } returns null
        every { mockPattern.matches(tree) } returns matchResult
        every { mockPattern.kind } returns instructionKind
        every { mockPattern.cost } returns 1

        val resultInstructions = instructionCoverer.cover(tree, null)

        assertEquals(
            listOf(mockInstruction),
            resultInstructions
        )
    }

    @Test
    fun `coverer should cover memoryTree`() {
        val child = NumericalConstantTree(2)
        // Arrange
        val tree = MemoryTree(child)
        val mockPattern = mockk<InstructionPattern>()
        val mockInstruction = mockk<Instruction>()
        val mockInstruction2 = mockk<Instruction>()

        val instructionPatterns = mapOf(
            InstructionPatternMapKey(tree.treeKind(), InstructionKind.EXEC) to listOf(mockPattern),
            InstructionPatternMapKey(child.treeKind(), InstructionKind.VALUE) to listOf(mockPattern),
        )
        val instructionCoverer = InstructionCoverer(instructionPatterns)
        val instructionKind = InstructionKind.VALUE

        val matchResult = InstructionMatchResult(
            children = listOf(child),
            createInstruction = { _, _, _ -> listOf(mockInstruction) }
        )

        val anyMatchResult = InstructionMatchResult(
            children = listOf(),
            createInstruction = { _, _, _ -> listOf(mockInstruction2) }
        )

        every { mockPattern.matches(any()) } returns anyMatchResult
        every { mockPattern.matches(tree) } returns matchResult
        every { mockPattern.kind } returns instructionKind
        every { mockPattern.cost } returns 1

        val resultInstructions = instructionCoverer.cover(tree, null)

        assertEquals(
            listOf(mockInstruction2, mockInstruction),
            resultInstructions
        )
    }


    @Test
    fun `cover should return cheapest cover`() {
        val tree = UnaryOperationTree(
            UnaryOperationTree(
                UnaryOperationTree(
                    UnaryOperationTree(
                        UnaryOperationTree(
                            UnaryOperationTree(
                                UnaryOperationTree(NumericalConstantTree(1), UnaryTreeOperationType.MINUS),
                                UnaryTreeOperationType.MINUS
                            ),
                            UnaryTreeOperationType.MINUS
                        ), UnaryTreeOperationType.MINUS
                    ), UnaryTreeOperationType.MINUS
                ), UnaryTreeOperationType.MINUS
            ), UnaryTreeOperationType.MINUS
        )
        val inst1 = mockk<Instruction>()
        val unaryOpPattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.VALUE
            override val cost: Int
                get() = 1

            override fun matches(parseTree: Tree): InstructionMatchResult? {
                if (parseTree !is UnaryOperationTree) {
                    return null
                }
                if (parseTree.child is UnaryOperationTree) {
                    return InstructionMatchResult(
                        listOf(parseTree.child),
                        createInstruction = { _, _, _ -> listOf(inst1) })
                }
                return InstructionMatchResult(
                    listOf(),
                    createInstruction = { _, _, _ -> listOf(inst1) })
            }
        }
        val inst3 = mockk<Instruction>()
        val unaryOpExpensivePattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.VALUE
            override val cost: Int
                get() = 100

            override fun matches(parseTree: Tree): InstructionMatchResult? {
                if (parseTree !is UnaryOperationTree) {
                    return null
                }
                if (parseTree.child is UnaryOperationTree) {
                    return InstructionMatchResult(
                        listOf((parseTree.child as UnaryOperationTree).child),
                        createInstruction = { _, _, _ -> listOf(inst3) })
                }
                return null
            }
        }
        val instructionCoverer =
            InstructionCoverer(
                mapOf(
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.EXEC) to listOf(
                        unaryOpExpensivePattern,
                        unaryOpPattern
                    ),
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.VALUE) to listOf(
                        unaryOpExpensivePattern,
                        unaryOpPattern
                    ),
                    InstructionPatternMapKey(ConstantTreeKind, InstructionKind.VALUE) to listOf(),
                )
            )
        assertEquals(
            listOf(inst1, inst1, inst1, inst1, inst1, inst1, inst1),
            instructionCoverer.cover(tree, null)
        )
    }

    @Test
    fun `cover should return one expensive but cheaper than many cheap`() {
        val tree = UnaryOperationTree(
            UnaryOperationTree(
                UnaryOperationTree(
                    UnaryOperationTree(
                        UnaryOperationTree(
                            UnaryOperationTree(
                                UnaryOperationTree(NumericalConstantTree(1), UnaryTreeOperationType.MINUS),
                                UnaryTreeOperationType.MINUS
                            ),
                            UnaryTreeOperationType.MINUS
                        ), UnaryTreeOperationType.MINUS
                    ), UnaryTreeOperationType.MINUS
                ), UnaryTreeOperationType.MINUS
            ), UnaryTreeOperationType.MINUS
        )
        val inst1 = mockk<Instruction>()
        val unaryOpPattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.VALUE
            override val cost: Int
                get() = 5

            override fun matches(parseTree: Tree): InstructionMatchResult? {
                if (parseTree !is UnaryOperationTree) {
                    return null
                }
                if (parseTree.child is UnaryOperationTree) {
                    return InstructionMatchResult(
                        listOf(parseTree.child),
                        createInstruction = { _, _, _ -> listOf(inst1) })
                }
                return InstructionMatchResult(
                    listOf(),
                    createInstruction = { _, _, _ -> listOf(inst1) })
            }
        }
        val inst2 = mockk<Instruction>()
        val unaryOpExpensivePattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.VALUE
            override val cost: Int
                get() = 10

            override fun matches(parseTree: Tree): InstructionMatchResult? {
                if (parseTree !is UnaryOperationTree) {
                    return null
                }
                if (parseTree.child is UnaryOperationTree) {
                    return InstructionMatchResult(
                        listOf((parseTree.child as UnaryOperationTree).child),
                        createInstruction = { _, _, _ -> listOf(inst2) })
                }
                return null
            }
        }
        val instructionCoverer =
            InstructionCoverer(
                mapOf(
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.EXEC) to listOf(
                        unaryOpExpensivePattern,
                        unaryOpPattern
                    ),
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.VALUE) to listOf(
                        unaryOpExpensivePattern,
                        unaryOpPattern
                    ),
                    InstructionPatternMapKey(ConstantTreeKind, InstructionKind.VALUE) to listOf(),
                )
            )
        assertEquals(
            listOf(inst1, inst2, inst2, inst2),
            instructionCoverer.cover(tree, null)
        )
    }


    @Test
    fun `cover should return good order`() {
        val tree =
            UnaryOperationTree(
                UnaryOperationTree(NumericalConstantTree(1), UnaryTreeOperationType.MINUS),
                UnaryTreeOperationType.MINUS
            )
        val inst1 = mockk<Instruction>()
        val inst2 = mockk<Instruction>()
        val unaryOpPattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.VALUE
            override val cost: Int
                get() = 1

            override fun matches(parseTree: Tree): InstructionMatchResult? {
                if (parseTree !is UnaryOperationTree) {
                    return null
                }
                if (parseTree.child is UnaryOperationTree) {
                    return InstructionMatchResult(
                        listOf(parseTree.child),
                        createInstruction = { _, _, _ -> listOf(inst1) })
                }
                return InstructionMatchResult(
                    listOf(),
                    createInstruction = { _, _, _ -> listOf(inst2) })
            }
        }
        val instructionCoverer =
            InstructionCoverer(
                mapOf(
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.EXEC) to listOf(unaryOpPattern),
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.VALUE) to listOf(unaryOpPattern),
                    InstructionPatternMapKey(ConstantTreeKind, InstructionKind.VALUE) to listOf(),
                )
            )
        assertEquals(
            listOf(inst2, inst1),
            instructionCoverer.cover(tree, null)
        )
    }


    @Test
    fun `cover of jump should use jump`() {
        val tree = NumericalConstantTree(1)

        val inst1 = mockk<Instruction>()
        val unaryOpPattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.VALUE
            override val cost: Int
                get() = 1

            override fun matches(parseTree: Tree): InstructionMatchResult {
                return InstructionMatchResult(
                    listOf(),
                    createInstruction = { _, _, _ -> listOf(inst1) })
            }
        }

        val inst2 = mockk<Instruction>()
        val unaryOpJumpPattern = object : InstructionPattern {
            override val rootType: TreeKind
                get() = mockk<TreeKind>()
            override val kind: InstructionKind
                get() = InstructionKind.JUMP
            override val cost: Int
                get() = 1

            override fun matches(parseTree: Tree): InstructionMatchResult {
                return InstructionMatchResult(
                    listOf(),
                    createInstruction = { _, _, _ -> listOf(inst2) })
            }
        }
        val instructionCoverer =
            InstructionCoverer(
                mapOf(
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.VALUE) to listOf(unaryOpPattern),
                    InstructionPatternMapKey(tree.treeKind(), InstructionKind.JUMP) to listOf(unaryOpJumpPattern),
                )
            )
        assertEquals(
            listOf(inst2),
            instructionCoverer.cover(tree, Label("labbbbeeel"))
        )
    }
}