package org.exeval.ast

import BinaryOperation
import Block
import BoolLiteral
import Conditional
import ConstantDeclaration
import FunctionDeclaration
import IntLiteral
import MutableVariableDeclaration
import UnaryOperation
import VariableReference
import io.mockk.every
import io.mockk.mockk
import org.exeval.input.SimpleLocation
import org.exeval.utilities.LocationRange
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TypeCheckerTest {

    @Test
    fun `should infer type for simple integer-returning function`() {
        // Simple code: `foo g() -> Int = { 3 }`
        val intLiteral = IntLiteral(3)
        val functionDeclaration = FunctionDeclaration(
            name = "g",
            parameters = emptyList(),
            returnType = IntType,
            body = intLiteral
        )

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns functionDeclaration
        every { mockAstInfo.locations } returns mapOf(intLiteral to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)))

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[intLiteral], "Expected IntLiteral type to be IntType")
        assertEquals(IntType, result.result[functionDeclaration], "Expected return type of function 'g' to be IntType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for a correctly typed function")
    }

    @Test
    fun `should detect correct types in conditional expression with logical operators`() {
        // Code: `foo main() -> Int = { if true and not false then 1 else 0 }`
        val trueLiteral = BoolLiteral(true)
        val falseLiteral = BoolLiteral(false)
        val notFalse = UnaryOperation(UnaryOperator.NOT, falseLiteral)
        val condition = BinaryOperation(trueLiteral, BinaryOperator.AND, notFalse)
        val thenBranch = IntLiteral(1)
        val elseBranch = IntLiteral(0)
        val conditionalExpr = Conditional(condition, thenBranch, elseBranch)

        val functionDeclaration = FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = conditionalExpr
        )

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns functionDeclaration
        every { mockAstInfo.locations } returns mapOf(condition to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)))

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[functionDeclaration], "Expected return type of main function to be IntType")
        assertEquals(BoolType, result.result[condition], "Expected condition in if statement to be BoolType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for a correctly typed if statement")
    }

    @Test
    fun `should infer types for function with variable declarations and conditional comparison`() {
        // Code:
        // ```
        // foo main() -> Int = {
        //     let mut a: Int;
        //     let mut b: Int;
        //     let max: Int = if a > b then a else b;
        // }
        // ```

        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType)
        val bDeclaration = MutableVariableDeclaration(name = "b", type = IntType)

        val aReference = VariableReference("a")
        val bReference = VariableReference("b")
        val comparison = BinaryOperation(aReference, BinaryOperator.GT, bReference)

        val conditionalExpr = Conditional(
            condition = comparison,
            thenBranch = aReference,
            elseBranch = bReference
        )

        val maxDeclaration = ConstantDeclaration(
            name = "max",
            type = IntType,
            initializer = conditionalExpr
        )

        val functionBody = Block(listOf(aDeclaration, bDeclaration, maxDeclaration))
        val functionDeclaration = FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = functionBody
        )

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns functionDeclaration
        every { mockAstInfo.locations } returns mapOf(
            aReference to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)),
            bReference to LocationRange(SimpleLocation(0, 2), SimpleLocation(0, 3)),
            comparison to LocationRange(SimpleLocation(1, 0), SimpleLocation(1, 1))
        )
        every { mockNameResolution.variableToDecl[aReference] } returns aDeclaration
        every { mockNameResolution.variableToDecl[bReference] } returns bDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[aReference], "Expected 'a' to be of type IntType")
        assertEquals(IntType, result.result[bReference], "Expected 'b' to be of type IntType")
        assertEquals(IntType, result.result[conditionalExpr], "Expected conditional expression to have type IntType")
        assertEquals(IntType, result.result[functionDeclaration], "Expected return type of main function to be IntType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for correctly typed conditional comparison")
    }
}