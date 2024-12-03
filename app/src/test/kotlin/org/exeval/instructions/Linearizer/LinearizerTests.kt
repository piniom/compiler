import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.instructions.Instruction
import org.exeval.instructions.InstructionCovererInterface
import org.exeval.instructions.linearizer.BasicBlock
import org.exeval.instructions.linearizer.Linearizer
import org.exeval.cfg.Tree
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

class LinearizerTest {

    @Test
    fun `test createBasicBlocks with null node`() {
        val mockInstructionCoverer = mockk<InstructionCovererInterface>()
        val linearizer = Linearizer(mockInstructionCoverer)

        val result = linearizer.createBasicBlocks(null)

        assertEquals(emptyList<BasicBlock>(), result, "Expected an empty list when input node is null.")
    }

    @Test
    fun `test createBasicBlocks with single CFGNode`() {
        val mockInstructionCoverer = mockk<InstructionCovererInterface>()
        val mockNode = mockk<CFGNode>()
        val mockInstruction = mockk<Instruction>()

        every { mockNode.trees } returns emptyList()
        every { mockNode.branches } returns null
        every { mockInstructionCoverer.cover(any()) } returns listOf(mockInstruction)

        val linearizer = Linearizer(mockInstructionCoverer)

        val result = linearizer.createBasicBlocks(mockNode)

        assertEquals(1, result.size, "Expected one basic block to be created.")
        assertEquals(0, result[0].instructions.size, "Expected no instructions as the node's trees are empty.")
        assertEquals(0, result[0].successors.size, "Expected no successors as the node has no branches.")
    }

    @Test
    fun `test createBasicBlocks with simple branch`() {
        val mockInstructionCoverer = mockk<InstructionCovererInterface>()
        val mockNode1 = mockk<CFGNode>()
        val mockNode2 = mockk<CFGNode>()
        val mockNode3 = mockk<CFGNode>()
        val mockInstruction1 = mockk<Instruction>()
        val mockInstruction2 = mockk<Instruction>()

        every { mockNode1.trees } returns listOf()
        every { mockNode2.trees } returns listOf()
        every { mockNode3.trees } returns listOf()
        every { mockInstructionCoverer.cover(any()) } returns listOf(mockInstruction1, mockInstruction2)

        every { mockNode1.branches } returns Pair(mockNode2, mockNode3)
        every { mockNode2.branches } returns null
        every { mockNode3.branches } returns null

        val linearizer = Linearizer(mockInstructionCoverer)

        val result = linearizer.createBasicBlocks(mockNode1)

        assertEquals(3, result.size, "Expected three basic blocks due to branches.")
        assertEquals(0, result[0].instructions.size, "Expected no instructions for the first block.")
        assertEquals(0, result[1].instructions.size, "Expected no instructions for the second block.")
        assertEquals(0, result[2].instructions.size, "Expected no instructions for the third block.")
        assertEquals(2, result[0].successors.size, "Expected two successors for the root block.")
    }

    @Test
    fun `test createBasicBlocks instruction cover is called`() {
        val mockInstructionCoverer = mockk<InstructionCovererInterface>()
        val mockNode = mockk<CFGNode>()
        val mockTree = mockk<Tree>()
        val mockInstruction = mockk<Instruction>()

        every { mockNode.trees } returns listOf(mockTree)
        every { mockNode.branches } returns null
        every { mockInstructionCoverer.cover(mockTree) } returns listOf(mockInstruction)

        val linearizer = Linearizer(mockInstructionCoverer)

        val result = linearizer.createBasicBlocks(mockNode)

        verify(exactly = 1) { mockInstructionCoverer.cover(mockTree) }
        assertEquals(1, result.size, "Expected one basic block.")
        assertEquals(1, result[0].instructions.size, "Expected one instruction in the basic block.")
        assertEquals(mockInstruction, result[0].instructions[0], "Expected the mocked instruction to be included.")
    }

    @Test
    fun `test createBasicBlocks with 15+ nodes and multiple instructions and branches`() {
        // Mock dependencies
        val mockInstructionCoverer = mockk<InstructionCovererInterface>()

        // Mock nodes
        val nodes = (1..15).map { mockk<CFGNode>("Node$it") }
        val trees = (1..15).map { mockk<Tree>("Tree$it") }
        val instructions = (1..15).map { mockk<Instruction>("Instruction$it") }
        val instructionSets = (1..15).map { getRandomNonEmptySubset(instructions) }

        // Define instructions for each tree
        trees.forEachIndexed { index, tree ->
            every { mockInstructionCoverer.cover(tree) } returns instructionSets[index]
        }

        // Mock tree and branch structure
        nodes.forEachIndexed { index, node ->
            every { node.trees } returns listOf(trees[index])
        }

        // Define the CFG structure
        every { nodes[0].branches } returns Pair(nodes[1], nodes[2])      // Node1 -> Node2, Node3
        every { nodes[1].branches } returns Pair(nodes[3], nodes[4])      // Node2 -> Node4, Node5
        every { nodes[2].branches } returns Pair(nodes[5], null)          // Node3 -> Node6
        every { nodes[3].branches } returns Pair(nodes[6], nodes[7])      // Node4 -> Node7, Node8
        every { nodes[4].branches } returns Pair(nodes[8], null)          // Node5 -> Node9
        every { nodes[5].branches } returns Pair(nodes[9], nodes[10])     // Node6 -> Node10, Node11
        every { nodes[6].branches } returns null                          // Node7 -> End
        every { nodes[7].branches } returns Pair(nodes[11], nodes[12])    // Node8 -> Node12, Node13
        every { nodes[8].branches } returns Pair(nodes[13], null)         // Node9 -> Node14
        every { nodes[9].branches } returns Pair(nodes[14], null)         // Node10 -> Node15
        every { nodes[10].branches } returns null                         // Node11 -> End
        every { nodes[11].branches } returns null                         // Node12 -> End
        every { nodes[12].branches } returns null                         // Node13 -> End
        every { nodes[13].branches } returns null                         // Node14 -> End
        every { nodes[14].branches } returns null                         // Node15 -> End

        /*
            BB BLOCKS should look like:
            BB1 -> Node1
            BB2 -> Node2
            BB3 -> Node4
            BB4 -> Node7
            BB5 -> Node8
            BB6 -> Node12
            BB7 -> Node13
            BB8 -> Node5
            BB9 -> Node9
            BB10 -> Node14
            BB11 -> Node3
            BB12 -> Node6
            BB13 -> Node10
            BB14 -> Node15
            BB15 -> Node11
        */

        // Create the Linearizer instance
        val linearizer = Linearizer(mockInstructionCoverer)

        // Call the method under test
        val result = linearizer.createBasicBlocks(nodes[0])

        // Assertions
        assertEquals(15, result.size, "Expected 15 basic blocks for the given CFG structure.")

        val blockNumToNodeNumMap = mapOf(
            0 to 0,
            1 to 1,
            2 to 3,
            3 to 6,
            4 to 7,
            5 to 11,
            6 to 12,
            7 to 4,
            8 to 8,
            9 to 13,
            10 to 2,
            11 to 5,
            12 to 9,
            13 to 14,
            14 to 10,
        )

        // Assert that all blocks have the correct set of instructions
        for (i in 0..14) {
            val expectedNodeIndex = blockNumToNodeNumMap[i]!!
            assertEquals(
                instructionSets[expectedNodeIndex],
                result[i].instructions,
                "Block ${i + 1} should have instructions from Node${expectedNodeIndex + 1}"
            )
        }

        // Ensure that all successors are correct
        for (i in 0..14) {
            var nodeIdx = blockNumToNodeNumMap[i]!!
            var branchesIdxs = mutableListOf<Int>()
            if (nodes[nodeIdx].branches != null) {
                branchesIdxs.add(nodes.indexOf(nodes[nodeIdx].branches!!.first!!))
                if (nodes[nodeIdx].branches!!.second != null) {
                    branchesIdxs.add(nodes.indexOf(nodes[nodeIdx].branches!!.second!!))
                }
            }

            assertEquals(
                result[i].successors.size,
                branchesIdxs.size,
                "coresponded BB and CFG node should have the same number of successors"
            )

            for (successor in result[i].successors) {
                var idx = result.indexOf(successor)
                assertEquals(true, branchesIdxs.contains(blockNumToNodeNumMap[idx]!!))
            }
        }
    }

    private fun <T> getRandomNonEmptySubset(list: List<T>): List<T> {
        val subsetSize = Random.nextInt(1, list.size + 1)
        return list.shuffled().take(subsetSize)
    }
}
