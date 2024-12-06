package org.exeval.ast

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

    @Test
    fun `should correctly infer type for valid function call with positional arguments`() {
        // Code: `foo main() -> Int = { add(1, 2) }`
        val arg1 = PositionalArgument(IntLiteral(1))
        val arg2 = PositionalArgument(IntLiteral(2))
        val functionCall = FunctionCall("add", listOf(arg1, arg2))

        val functionDeclaration = FunctionDeclaration(
            name = "add",
            parameters = listOf(
                Parameter("x", IntType),
                Parameter("y", IntType)
            ),
            returnType = IntType,
            body = IntLiteral(0)
        )

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns functionCall
        every { mockAstInfo.locations } returns mapOf(functionCall to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)))
        every { mockNameResolution.functionToDecl[functionCall] } returns functionDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[functionCall], "Expected function call to have return type IntType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for correctly typed positional arguments")
    }

    @Test
    fun `should detect error for function call with mixed argument types`() {
        // Code: `foo main() -> Int = { add(x=1, 2) }`
        val namedArg = NamedArgument("x", IntLiteral(1))
        val positionalArg = PositionalArgument(IntLiteral(2))
        val functionCall = FunctionCall("add", listOf(namedArg, positionalArg))

        val functionDeclaration = FunctionDeclaration(
            name = "add",
            parameters = listOf(
                Parameter("x", IntType),
                Parameter("y", IntType)
            ),
            returnType = IntType,
            body = IntLiteral(0)
        )

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns functionCall
        every { mockAstInfo.locations } returns mapOf(functionCall to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)))
        every { mockNameResolution.functionToDecl[functionCall] } returns functionDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[functionCall], "Expected function call type to be IntType")
        assertEquals(0, result.diagnostics.size, "Expected diagnostic for mixed argument types")
    }

    // Assigment
    @Test
    fun `should report error for block with mismatched types`() {
        // Code:
        // ```
        // {
        //     let mut a: Int;
        //     let b: Bool = true;
        //     a = b;
        // }
        // ```

        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType)
        val bDeclaration = ConstantDeclaration(name = "b", type = BoolType, initializer = BoolLiteral(true))
        val assignment = Assignment(variable = "a", value = VariableReference("b"))
        val block = Block(listOf(aDeclaration, bDeclaration, assignment))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations } returns mapOf(
            assignment to LocationRange(SimpleLocation(2, 5), SimpleLocation(2, 9))
        )
        every { mockNameResolution.variableToDecl[VariableReference("b")] } returns bDeclaration
        every { mockNameResolution.variableToDecl[VariableReference("a")] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for invalid assignment in block")
        assertEquals(
            "Assignment type does not match variable type",
            result.diagnostics[0].message,
            "Expected diagnostic message for type mismatch in assignment"
        )
    }


    // If statement
    @Test
    fun `should report error for conditional with mismatched then and else types`() {
        // Code: `if true then 1 else false`
        val condition = BoolLiteral(true)
        val thenBranch = IntLiteral(1)
        val elseBranch = BoolLiteral(false)
        val conditionalExpr = Conditional(condition, thenBranch, elseBranch)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns conditionalExpr
        every { mockAstInfo.locations } returns mapOf(
            conditionalExpr to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 10))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for mismatched then and else types")
        assertEquals(
            "Then and else branches must have the same type",
            result.diagnostics[0].message,
            "Expected diagnostic message for mismatched branches"
        )
    }

    @Test
    fun `should report error for conditional with non-Bool condition`() {
        // Code: `if 42 then 1 else 0`
        val condition = IntLiteral(42)
        val thenBranch = IntLiteral(1)
        val elseBranch = IntLiteral(0)
        val conditionalExpr = Conditional(condition, thenBranch, elseBranch)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns conditionalExpr
        every { mockAstInfo.locations } returns mapOf(
            conditionalExpr to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 10))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for non-Bool condition")
        assertEquals(
            "Condition expression must be Bool",
            result.diagnostics[0].message,
            "Expected diagnostic message for non-Bool condition"
        )
    }

    @Test
    fun `should report error for nested conditional with mismatched types`() {
        // Code:
        // ```
        // if true then
        //     if false then 1 else true
        // else 0
        // ```

        val innerCondition = BoolLiteral(false)
        val innerThenBranch = IntLiteral(1)
        val innerElseBranch = BoolLiteral(true)
        val innerConditional = Conditional(innerCondition, innerThenBranch, innerElseBranch)

        val outerCondition = BoolLiteral(true)
        val outerThenBranch = innerConditional
        val outerElseBranch = IntLiteral(0)
        val outerConditional = Conditional(outerCondition, outerThenBranch, outerElseBranch)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns outerConditional
        every { mockAstInfo.locations } returns mapOf(
            innerConditional to LocationRange(SimpleLocation(1, 0), SimpleLocation(1, 15)),
            outerConditional to LocationRange(SimpleLocation(0, 0), SimpleLocation(2, 5))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(2, result.diagnostics.size, "Expected two diagnostics for nested conditional with mismatched types")
        assertEquals(
            "Then and else branches must have the same type",
            result.diagnostics[0].message,
            "Expected diagnostic message for inner conditional type mismatch"
        )
        assertEquals(
            "Then and else branches must have the same type",
            result.diagnostics[1].message,
            "Expected diagnostic message for outer conditional type mismatch"
        )
    }

    @Test
    fun `should report error for assignment in conditional condition`() {
        // Code: `if (x = 42) then 1 else 0`
        val assignmentInCondition = Assignment(variable = "x", value = IntLiteral(42))
        val condition = assignmentInCondition
        val thenBranch = IntLiteral(1)
        val elseBranch = IntLiteral(0)
        val conditionalExpr = Conditional(condition, thenBranch, elseBranch)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns conditionalExpr
        every { mockAstInfo.locations } returns mapOf(
            conditionalExpr to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 15))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for assignment in condition")
        assertEquals(
            "Condition expression must be Bool",
            result.diagnostics[0].message,
            "Expected diagnostic message for invalid assignment in condition"
        )
    }


    // Loops
    @Test
    fun `should report error for loop with NopeType body`() {
        // Code: `loop { NopeLiteral }`
        val loopBody = NopeLiteral
        val loop = Loop(identifier = null, body = loopBody)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns loop
        every { mockAstInfo.locations } returns mapOf(
            loop to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 6))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for NopeType loop body")
        assertEquals(
            "Loop body cannot be of NopeType",
            result.diagnostics[0].message,
            "Expected diagnostic message for NopeType body"
        )
    }

}