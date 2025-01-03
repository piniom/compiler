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
    fun `should infer type for foreign integer-returning function`() {
        // Simple code: `foreign foo g() -> Int`
        val functionDeclaration = ForeignFunctionDeclaration(
            name = "g",
            parameters = emptyList(),
            returnType = IntType
        )

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns functionDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
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
    fun `should type-check simplest assignment`() {
        // Code:
        // ```
        // {
        //     let mut a: Int = 0;
        // }
        // ```

        // AST construction
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType, initializer = IntLiteral(0))
        val block = Block(listOf(aDeclaration))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }
        every { mockNameResolution.variableToDecl[VariableReference("a")] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for valid mutable variable declaration")
        assertEquals(
            NopeType,
            result.result[aDeclaration],
            "Expected the type of variable 'a' declaration to be NopeType"
        )
    }

    @Test
    fun `should type-check declaration and assignment expressions with NopeType`() {
        // Code:
        // ```
        // {
        //     let mut a: Int = 0;
        //     a = 42;
        // }
        // ```

        // AST Construction
        val aReference = VariableReference("a")
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType, initializer = IntLiteral(0))
        val aAssignment = Assignment(variable = aReference, value = IntLiteral(42))
        val block = Block(listOf(aDeclaration, aAssignment))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }
        every { mockNameResolution.variableToDecl[aReference] } returns aDeclaration
        every { mockNameResolution.assignmentToDecl[aAssignment] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for valid declaration and assignment expressions")
        assertEquals(
            NopeType,
            result.result[aDeclaration],
            "Expected the type of the declaration expression to be NopeType"
        )
        assertEquals(
            NopeType,
            result.result[aAssignment],
            "Expected the type of the assignment expression to be NopeType"
        )
    }


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

        val aReference = VariableReference("a")
        val bReference = VariableReference("b")
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType)
        val bDeclaration = ConstantDeclaration(name = "b", type = BoolType, initializer = BoolLiteral(true))
        val assignment = Assignment(variable = aReference, value = bReference)
        val block = Block(listOf(aDeclaration, bDeclaration, assignment))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }
        every { mockNameResolution.variableToDecl[bReference] } returns bDeclaration
        every { mockNameResolution.variableToDecl[aReference] } returns aDeclaration
        every { mockNameResolution.assignmentToDecl[assignment] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for invalid assignment in block")
        assertEquals(
            "Assignment type does not match variable type",
            result.diagnostics[0].message,
            "Expected diagnostic message for mismatched types in assignment"
        )
    }

    @Test
    fun `should type-check loop with assignment and break returning a value`() {
        // Code:
        // ```
        // {
        //     let mut a: Int = 0;
        //     a = loop {
        //         a = a + 1;
        //         break a;
        //     };
        // }
        // ```

        // AST construction
        val aReference = VariableReference("a")
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType, initializer = IntLiteral(0))
        val increment = BinaryOperation(aReference, BinaryOperator.PLUS, IntLiteral(1))
        val assignmentInsideLoop = Assignment(variable = aReference, value = increment)
        val breakStatement = Break(null, aReference)
        val loopBody = Block(listOf(assignmentInsideLoop, breakStatement))
        val loop = Loop(null, loopBody)
        val outerAssignment = Assignment(variable = aReference, value = loop)
        val block = Block(listOf(aDeclaration, outerAssignment))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }
        every { mockNameResolution.variableToDecl[aReference] } returns aDeclaration
        every { mockNameResolution.assignmentToDecl[outerAssignment] } returns aDeclaration
        every { mockNameResolution.assignmentToDecl[assignmentInsideLoop] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for valid loop with assignment and break")
        assertEquals(
            NopeType,
            result.result[aDeclaration],
            "Expected the declaration of 'a' to have type NopeType"
        )
        assertEquals(
            IntType,
            result.result[loop],
            "Expected the loop to have type IntType"
        )
        assertEquals(
            NopeType,
            result.result[outerAssignment],
            "Expected the assignment to have type NopeType"
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
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }

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
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for non-Bool condition")
        assertEquals(
            "Condition expression must be Bool",
            result.diagnostics[0].message,
            "Expected diagnostic message for non-Bool condition in if statement"
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
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected two diagnostics for nested conditional with mismatched types")
        assertEquals(
            "Then and else branches must have the same type",
            result.diagnostics[0].message,
            "Expected diagnostic message for mismatched branches in inner conditional"
        )
    }

    @Test
    fun `should pass for if without else returning NopeType like in assigment`() {
        // Code:
        // ```
        // let mut a: Int = 0;
        // if true {
        //     a = 1;
        // }
        // ```

        // AST Construction
        val aReference = VariableReference("a")
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType, initializer = IntLiteral(0))
        val aAssignment = Assignment(variable = aReference, value = IntLiteral(1))
        val ifStatement = Conditional(
            condition = BoolLiteral(true),
            thenBranch = Block(listOf(aAssignment)),
            elseBranch = null
        )
        val block = Block(listOf(aDeclaration, ifStatement))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }
        every { mockNameResolution.variableToDecl[aReference] } returns aDeclaration
        every { mockNameResolution.assignmentToDecl[aAssignment] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(0, result.diagnostics.size, "Expected one diagnostic for invalid if without else")
    }

    @Test
    fun `should report error for if without else not returning NopeType`() {
        // Code:
        // ```
        // let mut a: Int = 0;
        // if true {
        //     1;
        // }
        // ```

        // AST Construction
        val aReference = VariableReference("a")
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntType, initializer = IntLiteral(0))
        val ifStatement = Conditional(
            condition = BoolLiteral(true),
            thenBranch = Block(listOf(IntLiteral(1))),
            elseBranch = null
        )
        val block = Block(listOf(aDeclaration, ifStatement))

        // Set up AstInfo and NameResolution
        val mockAstInfo = mockk<AstInfo>()
        val mockNameResolution = mockk<NameResolution>()
        every { mockAstInfo.root } returns block
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }
        every { mockNameResolution.variableToDecl[aReference] } returns aDeclaration

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for invalid if without else")
        assertEquals(
            "Condition expression without else must be a Nope type",
            result.diagnostics[0].message,
            "Expected diagnostic message for invalid if without else"
        )
    }

    // Loops
    @Test
    fun `should infer type for simple loop with unlabeled break`() {
        // Code: loop { break 2; }
        val breakStmt = Break(null, IntLiteral(2))
        val loop = Loop(null, breakStmt)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns loop

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockk())
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[loop], "Expected loop to evaluate to IntType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for valid loop with unlabeled break")
    }

    @Test
    fun `should handle nested labeled loops with break targeting outer loop`() {
        // Code:
        // loop@secondLoop {
        //     loop@firstLoop {
        //         break@firstLoop 3;
        //     }
        //     break 2;
        // }
        val breakToFirstLoop = Break("firstLoop", IntLiteral(3))
        val innerLoop = Loop("firstLoop", breakToFirstLoop)
        val breakStmt = Break(null, IntLiteral(2))
        val outerLoop = Loop("secondLoop", Block(listOf(innerLoop, breakStmt)))

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns outerLoop

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockk())
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[outerLoop], "Expected outer loop to evaluate to IntType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for valid nested labeled loops")
    }

    @Test
    fun `should infer type for loop with nested break expressions`() {
        // Code:
        // loop {
        //     break (3 * loop {
        //         break 2;
        //     });
        // }
        val innerBreak = Break(null, IntLiteral(2))
        val innerLoop = Loop(null, innerBreak)
        val multiplyExpr = BinaryOperation(IntLiteral(3), BinaryOperator.MULTIPLY, innerLoop)
        val outerBreak = Break(null, multiplyExpr)
        val outerLoop = Loop(null, outerBreak)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns outerLoop

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockk())
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[outerLoop], "Expected outer loop to evaluate to IntType")
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for valid nested break expressions")
    }

    @Test
    fun `should report error for break targeting non-existent loop label`() {
        // Code:
        // loop {
        //     break@firstLoop 1;
        // }
        val breakToInvalidLabel = Break("firstLoop", IntLiteral(1))
        val loop = Loop(null, breakToInvalidLabel)

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns loop
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }


        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockk())
        val result = typeChecker.parse()

        // Assertions
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for break targeting non-existent label")
        assertEquals(
            "Break targets an unknown or invalid loop",
            result.diagnostics[0].message,
            "Expected diagnostic message for break targeting non-existent loop label"
        )
    }

    @Test
    fun `should report error for loop with inconsistent break types`() {
        // Code:
        // loop {
        //     if true then {
        //         break 1;
        //     }
        //     break false;
        // }
        val breakInt = Break(null, IntLiteral(1))
        val breakBool = Break(null, BoolLiteral(false))
        val conditional = Conditional(BoolLiteral(true), Block(listOf(breakInt)), null)
        val loop = Loop(null, Block(listOf(conditional, breakBool)))

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns loop
        every { mockAstInfo.locations[any()] } answers {
            LocationRange(SimpleLocation(1, 4), SimpleLocation(1, 18))
        }

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockk())
        val result = typeChecker.parse()

        // Assertions
        assertEquals(2, result.diagnostics.size, "Expected one diagnostic for inconsistent break types")
        assertEquals(
            "Condition expression without else must be a Nope type",
            result.diagnostics[0].message,
            "Expected diagnostic message for conditional without else not returning Nope type"
        )
        assertEquals(
            "Break type does not match loop type",
            result.diagnostics[1].message,
            "Expected diagnostic message for conditional without else not returning Nope type"
        )
    }

    @Test
    fun `should allow infinite loop without break statements`() {
        // Code:
        // loop {
        //     if true then {
        //         let x: Int = 42;
        //     }
        //     let y: Int = 5 + 7;
        // }
        val intLiteral42 = IntLiteral(42)
        val letX = ConstantDeclaration(name = "x", type = IntType, initializer = intLiteral42)
        val ifStatement = Conditional(
            condition = BoolLiteral(true),
            thenBranch = Block(listOf(letX)),
            elseBranch = null
        )

        val addExpr = BinaryOperation(IntLiteral(5), BinaryOperator.PLUS, IntLiteral(7))
        val letY = ConstantDeclaration(name = "y", type = IntType, initializer = addExpr)
        val loop = Loop(null, Block(listOf(ifStatement, letY)))

        // Set up AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns loop
        every { mockAstInfo.locations } returns mapOf(
            letX to LocationRange(SimpleLocation(2, 4), SimpleLocation(2, 18)),
            letY to LocationRange(SimpleLocation(3, 4), SimpleLocation(3, 18)),
            loop to LocationRange(SimpleLocation(1, 4), SimpleLocation(4, 18)),
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockk())
        val result = typeChecker.parse()

        // Assertions
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics for infinite loop without break statements")
        assertEquals(
            NopeType,
            result.result[loop],
            "Expected the loop type to be NopeType for infinite loop without break statements"
        )
    }
}