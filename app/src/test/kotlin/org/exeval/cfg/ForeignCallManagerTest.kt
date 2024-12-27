package org.exeval.cfg

import io.mockk.*
import org.exeval.cfg.ffm.interfaces.CallManager
import org.exeval.cfg.interfaces.CFGNode
import org.exeval.ast.ForeignFunctionDeclaration
import org.exeval.ast.Type
import org.exeval.ast.IntType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ForeignCallManagerTest {

    @Test 
    fun `test generate_function_call with no arguments and no result`() {
        val function = ForeignFunctionDeclaration("mockFunctionName", listOf(), IntType)
        val callManager = ForeignCallManager(function)

        // args
        val trees = listOf<Tree>()
        val then: CFGNode = mockk()

        assertEquals(
            CFGNodeImpl(
                Pair(then, null),
                listOf<Tree>(
                    // Call function
                    Call(callManager.functionLabel)
                )
            ),
            callManager.generate_function_call(
                trees, null, then
            )
        )
    }

    @Test 
    fun `test generate_function_call with no arguments but result`() {
        val function = ForeignFunctionDeclaration("mockFunctionName", listOf(), IntType)
        val callManager = ForeignCallManager(function)

        // args
        val trees = listOf<Tree>()

        // Return
        val returnDest = MemoryTree(RegisterTree(VirtualRegister()))
        val then: CFGNode = mockk()

        assertEquals(
            CFGNodeImpl(
                Pair(then, null),
                listOf<Tree>(
                    // Call function
                    Call(callManager.functionLabel),
                    // Save result
                    AssignmentTree(
                        returnDest,
                        RegisterTree(PhysicalRegister.RAX)
                    )
                )
            ),
            callManager.generate_function_call(
                trees, returnDest, then
            )
        )
    }

    @Test
    fun `test generate_function_call with no result but some args provided`() {
        val function = ForeignFunctionDeclaration("mockFunctionName", listOf(), IntType)
        val callManager = ForeignCallManager(function)
        val reg1 = VirtualRegister()
        val reg2 = VirtualRegister()
        val reg3 = VirtualRegister()
        // args
        val trees = listOf<Tree>(
            NumericalConstantTree(2),
            RegisterTree(reg1),
            BinaryOperationTree(NumericalConstantTree(6), RegisterTree(reg2), BinaryTreeOperationType.MULTIPLY)
        )
        val then: CFGNode = mockk()

        assertEquals(
            CFGNodeImpl(
                Pair(then, null),
                listOf<Tree>(
                    // arg 1
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RDI),
                        NumericalConstantTree(2)
                    ),                    
                    // arg 2
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RSI),
                        RegisterTree(reg1)
                    ),
                    // arg3
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RDX),
                        BinaryOperationTree(
                            NumericalConstantTree(6),
                            RegisterTree(reg2),
                            BinaryTreeOperationType.MULTIPLY
                        )
                    ),
                    // Call function
                    Call(callManager.functionLabel)
                )
            ),
            callManager.generate_function_call(
                trees, null, then
            )
        )
    }

    @Test
    fun `test generate_function_call with result and some args provided`() {
        val function = ForeignFunctionDeclaration("mockFunctionName", listOf(), IntType)
        val callManager = ForeignCallManager(function)
        val reg1 = VirtualRegister()
        val reg2 = VirtualRegister()
        val reg3 = VirtualRegister()
        // args
        val trees = listOf<Tree>(
            NumericalConstantTree(2),
            RegisterTree(reg1),
            BinaryOperationTree(NumericalConstantTree(6), RegisterTree(reg2), BinaryTreeOperationType.MULTIPLY)
        )

        // Return
        val returnDest = MemoryTree(RegisterTree(reg3))
        val then: CFGNode = mockk()

        assertEquals(
            CFGNodeImpl(
                Pair(then, null),
                listOf<Tree>(
                    // arg 1
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RDI),
                        NumericalConstantTree(2)
                    ),                    
                    // arg 2
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RSI),
                        RegisterTree(reg1)
                    ),
                    // arg3
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RDX),
                        BinaryOperationTree(
                            NumericalConstantTree(6),
                            RegisterTree(reg2),
                            BinaryTreeOperationType.MULTIPLY
                        )
                    ),
                    // Call function
                    Call(callManager.functionLabel),
                    // Save result
                    AssignmentTree(
                        returnDest,
                        RegisterTree(PhysicalRegister.RAX)
                    )
                )
            ),
            callManager.generate_function_call(
                trees, returnDest, then
            )
        )
    }

    @Test
    fun `test generate_function_call with stack used`() {

        val function = ForeignFunctionDeclaration("mockFunctionName", listOf(), IntType)
        val callManager = ForeignCallManager(function)
        // args
        val trees = (0 until 10).map { it -> NumericalConstantTree(it.toLong()) }

        // Return
        val returnDest = MemoryTree(RegisterTree(VirtualRegister()))
        val then: CFGNode = mockk()

        assertEquals(
            CFGNodeImpl(
                Pair(then, null),
                listOf<Tree>(
                    // arg 0
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RDI),
                        NumericalConstantTree(0)
                    ),   
                    // arg 1
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RSI),
                        NumericalConstantTree(1)
                    ),   
                    // arg 2
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RDX),
                        NumericalConstantTree(2)
                    ),   
                    // arg 3
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.RCX),
                        NumericalConstantTree(3)
                    ),  
                    // arg 4 
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.R8),
                        NumericalConstantTree(4)
                    ),   
                    // arg 5
                    AssignmentTree(
                        RegisterTree(PhysicalRegister.R9),
                        NumericalConstantTree(5)
                    ),   
                    // push the rest arguments on stack
                    // arg 6
                    BinaryOperationTree(
                        RegisterTree(PhysicalRegister.RSP),
                        NumericalConstantTree(8),
                        BinaryTreeOperationType.SUBTRACT
                    ),
                    AssignmentTree(
                        MemoryTree(RegisterTree(PhysicalRegister.RSP)),
                        NumericalConstantTree(6)
                    ),
                    // arg 7
                    BinaryOperationTree(
                        RegisterTree(PhysicalRegister.RSP),
                        NumericalConstantTree(8),
                        BinaryTreeOperationType.SUBTRACT
                    ),
                    AssignmentTree(
                        MemoryTree(RegisterTree(PhysicalRegister.RSP)),
                        NumericalConstantTree(7)
                    ),
                    // arg 8
                    BinaryOperationTree(
                        RegisterTree(PhysicalRegister.RSP),
                        NumericalConstantTree(8),
                        BinaryTreeOperationType.SUBTRACT
                    ),
                    AssignmentTree(
                        MemoryTree(RegisterTree(PhysicalRegister.RSP)),
                        NumericalConstantTree(8)
                    ),
                    // arg 9
                    BinaryOperationTree(
                        RegisterTree(PhysicalRegister.RSP),
                        NumericalConstantTree(8),
                        BinaryTreeOperationType.SUBTRACT
                    ),
                    AssignmentTree(
                        MemoryTree(RegisterTree(PhysicalRegister.RSP)),
                        NumericalConstantTree(9)
                    ),
                    // Call function
                    Call(callManager.functionLabel),
                    // Save result
                    AssignmentTree(
                        returnDest,
                        RegisterTree(PhysicalRegister.RAX)
                    )
                )
            ),
            callManager.generate_function_call(
                trees, returnDest, then
            )
        )
    }
}
