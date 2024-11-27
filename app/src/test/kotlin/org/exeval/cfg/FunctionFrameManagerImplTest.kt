package org.exeval.cfg

import io.mockk.every
import io.mockk.mockk
import org.exeval.ast.AnyVariable
import org.exeval.ast.FunctionAnalysisResult
import org.exeval.ast.FunctionDeclaration
import org.exeval.cfg.constants.Registers
import org.exeval.cfg.interfaces.UsableMemoryCell
import org.exeval.cfg.interfaces.CFGNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FunctionFrameManagerImplTest {

    private lateinit var analyser: FunctionAnalysisResult
    private lateinit var functionDeclaration: FunctionDeclaration
    private lateinit var frameManager: FunctionFrameManagerImpl

    @BeforeEach
    fun setup() {
        analyser = mockk()
        functionDeclaration = mockk()

        every { analyser.variableMap } returns mutableMapOf()
        every { analyser.isUsedInNested } returns mutableMapOf()

        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)
    }

    @Test
    fun `test variable allocation for main with max`() {
        // foo main() -> Int = {
        //     let mut a: Int;
        //     let mut b: Int;
        //     let max: Int = {
        //         if(a>b) then {
        //             a;
        //         } else {
        //             b;
        //         };
        //     };
        // }

        // Mock variables
        val a = mockk<AnyVariable>()
        val b = mockk<AnyVariable>()
        val max = mockk<AnyVariable>()

        // Set up the mock analyser
        every { analyser.variableMap } returns mapOf(a to functionDeclaration, b to functionDeclaration, max to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(a to false, b to false, max to true)

        // Initialize the FunctionFrameManagerImpl
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // Check allocation for each variable
        assertEquals(UsableMemoryCell.VirtReg(4), frameManager.variable_to_virtual_register(a))
        assertEquals(UsableMemoryCell.VirtReg(5), frameManager.variable_to_virtual_register(b))
        assertEquals(UsableMemoryCell.MemoryPlace(0), frameManager.variable_to_virtual_register(max))
    }

    @Test
    fun `test variable allocation for if-else example`() {
        // foo main() -> Int = {
        //     let y: Int = if 5 > 3 then {
        //         let a: Int = 10;
        //         a + 5
        //     } else {
        //         let b: Int = 20;
        //         b - 5
        //     };
        //     y
        // }

        // Mock variables
        val y = mockk<AnyVariable>()
        val a = mockk<AnyVariable>()
        val b = mockk<AnyVariable>()

        // Set up the mock analyser
        every { analyser.variableMap } returns mapOf(y to functionDeclaration, a to functionDeclaration, b to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(y to false, a to false, b to false)

        // Initialize the FunctionFrameManagerImpl
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // Check allocation for each variable
        assertEquals(UsableMemoryCell.VirtReg(4), frameManager.variable_to_virtual_register(y))
        assertEquals(UsableMemoryCell.VirtReg(5), frameManager.variable_to_virtual_register(a))
        assertEquals(UsableMemoryCell.VirtReg(6), frameManager.variable_to_virtual_register(b))
    }

    @Test
    fun `test recursive function fib`() {
        // foo fib(n: Int) -> Int = {
        //     if n < 3 then {
        //         1
        //     } else {
        //         fib(n-1) + fib(n-2)
        //     }
        // }

        // Mock variables
        val n = mockk<AnyVariable>()

        // Set up the mock analyser
        every { analyser.variableMap } returns mapOf(n to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(n to false)

        // Initialize the FunctionFrameManagerImpl
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // Check allocation for `n`
        assertEquals(UsableMemoryCell.VirtReg(4), frameManager.variable_to_virtual_register(n))
    }

    @Test
    fun `test recursive function main calling fib`() {
        // foo main() -> Int = {
        //     fib(5)
        // }

        // Mock variables
        val fibResult = mockk<AnyVariable>()

        // Set up the mock analyser
        every { analyser.variableMap } returns mapOf(fibResult to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(fibResult to false)

        // Initialize the FunctionFrameManagerImpl
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // Check allocation for `fibResult`
        assertEquals(UsableMemoryCell.VirtReg(4), frameManager.variable_to_virtual_register(fibResult))
    }

    @Test
    fun `test variable x in outer function accessed in inner function`() {
        // foo f() = {
        //     let x: Int = 0
        //
        //     foo g() = {
        //         x += 1
        //     }
        // }

        // Mock variable `x`
        val x = mockk<AnyVariable>()

        // Update analyser mock specifically for this test case
        every { analyser.variableMap } returns mapOf(x to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(x to true) // Mark `x` as used in a nested scope

        // Reinitialize FunctionFrameManagerImpl after setting up specific mocks
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // Check allocation for `x`
        assertEquals(UsableMemoryCell.MemoryPlace(0), frameManager.variable_to_virtual_register(x))
    }

    @Test
    fun `generate function call no arguments no return`() {
        val x = mockk<AnyVariable>()
        every { analyser.variableMap } returns mapOf(x to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(x to false)
        val then = mockk<CFGNode>()

        // Reinitialize FunctionFrameManagerImpl after setting up specific mocks
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // args
        val trees = listOf<Tree>()

        assertEquals(
            frameManager.generate_function_call(
                trees, null, then
            ),
            CFGNodeImpl(
                Pair(then, null),
                listOf(
                    Call
                )
            )
        )
    }

    @Test
    fun `generate function call 3 arguments with return`() {
        val x = mockk<AnyVariable>()
        every { analyser.variableMap } returns mapOf(x to functionDeclaration)
        every { analyser.isUsedInNested } returns mapOf(x to false)
        val then = mockk<CFGNode>()

        // Reinitialize FunctionFrameManagerImpl after setting up specific mocks
        frameManager = FunctionFrameManagerImpl(functionDeclaration, analyser)

        // args
        val trees = listOf<Tree>(
            ConstantTree(2),
            VirtualRegisterTree(5),
            BinaryOperationTree(ConstantTree(6), VirtualRegisterTree(6), BinaryTreeOperationType.MULTIPLY)
        )

        // Return
        val returnDest = MemoryTree(VirtualRegisterTree(7))

        assertEquals(
            CFGNodeImpl(
                Pair(then, null),
                listOf<Tree>(
                    // arg 1
                    AssignmentTree(
                        PhysicalRegisterTree(Registers.RCX),
                        ConstantTree(2)
                    ),
                    // arg 2
                    AssignmentTree(
                        PhysicalRegisterTree(Registers.RDX),
                        VirtualRegisterTree(5)
                    ),
                    // push arg 3 on stack
                    BinaryOperationTree(PhysicalRegisterTree(Registers.RSP), ConstantTree(8), BinaryTreeOperationType.SUBTRACT),
                    AssignmentTree(
                        MemoryTree(PhysicalRegisterTree(Registers.RSP)),
                        BinaryOperationTree(ConstantTree(6), VirtualRegisterTree(6), BinaryTreeOperationType.MULTIPLY)
                    ),
                    // Call function
                    Call,
                    // Save result
                    AssignmentTree(
                        returnDest,
                        PhysicalRegisterTree(Registers.RAX)
                    )
                )
            ),
            frameManager.generate_function_call(
                trees, returnDest, then
            )
        )
    }
}
