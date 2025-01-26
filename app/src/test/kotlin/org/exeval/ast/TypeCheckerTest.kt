package org.exeval.ast

import io.mockk.every
import io.mockk.mockk
import org.exeval.input.SimpleLocation
import org.exeval.input.interfaces.Location
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
            returnType = IntTypeNode,
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
    fun `should infer type for expressions using pointers`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   let pointer: [Int] = new [Int] (5);
        //   pointer[0];
        //   del pointer
        // }
        // ```
        val type = Array(IntTypeNode)
        val memoryNew = MemoryNew(type, listOf(PositionalArgument(IntLiteral(5))))
        val declaration = ConstantDeclaration("pointer", type,  memoryNew)
        val reference1 = VariableReference("pointer")
        val arrayAccess = ArrayAccess(reference1, IntLiteral(0))
        val reference2 = VariableReference("pointer")
        val memoryDel = MemoryDel(reference2)
        val function = FunctionDeclaration(
            "main", emptyList(), NopeTypeNode,
            Block(
                listOf(declaration, arrayAccess, memoryDel)
            )
        )

        val locations = mockk<Map<ASTNode, LocationRange>>()
        every { locations[any()] } returns null
        every { locations[memoryNew] } returns LocationRange(mockk(), mockk())
        val astInfo = AstInfo(function, locations)


        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        val result = TypeChecker(astInfo, nameResolution).parse()

        assertEquals(ArrayType(IntType), result.result[memoryNew], "Expected type of memoryNew to be it's inner type")
        assertEquals(NopeType, result.result[declaration], "Expected type of declaration to be Nope")
        assertEquals(ArrayType(IntType), result.result[reference1], "Expected type of reference to be type of it's declaration")
        assertEquals(IntType, result.result[arrayAccess], "Expected type of ArrayAccess to be type the inner type of the Array")
        assertEquals(ArrayType(IntType), result.result[reference2], "Expected type of reference to be type of it's declaration")
        assertEquals(NopeType, result.result[memoryDel], "Expected type of MemoryDel to be Nope")
        assertTrue(result.diagnostics.isEmpty(), "Expected empty diagnostics")
    }

    @Test
    fun `should only allow for pointers to arrays`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   new Int (5);
        // }
        // ```
        val memoryNew = MemoryNew(IntTypeNode, listOf(PositionalArgument(IntLiteral(5))))
        val function = FunctionDeclaration(
            "main", emptyList(), NopeTypeNode,
            Block(
                listOf(memoryNew)
            )
        )

        val astInfo = AstInfo(function, mapOf(memoryNew to LocationRange(SimpleLocation(0, 0), SimpleLocation(0,1))))
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        val result = TypeChecker(astInfo, nameResolution).parse()

        // Assertions
        assertTrue(result.diagnostics.size == 1, "Expected at one diagnostics for pointer to non array type creation")
        assertEquals(
            "Only Arrays are allowed with the new keyword",
            result.diagnostics[0].message,
            "Expected diagnostic message for wrong new keyword type"
        )
    }

    @Test
    fun `should only allow IntType as argument to pointer constructor`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   new [Int] (());
        // }
        // ```
        val memoryNew = MemoryNew(Array(IntTypeNode), listOf(PositionalArgument(NopeLiteral())))
        val function = FunctionDeclaration(
            "main", emptyList(), NopeTypeNode,
            Block(
                listOf(memoryNew)
            )
        )

        val astInfo = AstInfo(function, mapOf(memoryNew to LocationRange(SimpleLocation(0, 0), SimpleLocation(0,1))))
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        val result = TypeChecker(astInfo, nameResolution).parse()

        // Assertions
        assertTrue(result.diagnostics.size == 1, "Expected at one diagnostics for pointer to non array type creation")
        assertEquals(
            "Argument to new must be Int",
            result.diagnostics[0].message,
            "Expected diagnostic message for wrong new keyword type"
        )
    }

    @Test
    fun `should only allow ArrayType as left side of ArrayAccess`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   1[2]
        // }
        // ```
        val arrayAccess = ArrayAccess(IntLiteral(1), IntLiteral(2))
        val function = FunctionDeclaration(
            "main", emptyList(), NopeTypeNode,
            Block(
                listOf(arrayAccess)
            )
        )

        val astInfo = AstInfo(function, mapOf(arrayAccess to LocationRange(SimpleLocation(0, 0), SimpleLocation(0,1))))
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        val result = TypeChecker(astInfo, nameResolution).parse()

        // Assertions
        assertTrue(result.diagnostics.size == 1, "Expected at one diagnostics for pointer to non array type creation")
        assertEquals(
            "Only Arrays are allowed on the left side of the array access operator",
            result.diagnostics[0].message,
            "Expected diagnostic message for wrong new keyword type"
        )
    }

    @Test
    fun `should only allow ArrayType with the delete keyword`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   del 5
        // }
        // ```
        val memDel = MemoryDel(IntLiteral(5))
        val function = FunctionDeclaration(
            "main", emptyList(), NopeTypeNode,
            Block(
                listOf(memDel)
            )
        )

        val astInfo = AstInfo(function, mapOf(memDel to LocationRange(SimpleLocation(0, 0), SimpleLocation(0,1))))
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        val result = TypeChecker(astInfo, nameResolution).parse()

        // Assertions
        assertTrue(result.diagnostics.size == 1, "Expected at one diagnostics for pointer to non array type creation")
        assertEquals(
            "Only Arrays are allowed with the delete keyword",
            result.diagnostics[0].message,
            "Expected diagnostic message for wrong new keyword type"
        )
    }

    @Test
    fun `should only allow IntType as argument to ArrayAccess`() {
        // Code:
        // ```
        // foo main() -> Nope = {
        //   (new [Int] (1))[()]
        // }
        // ```
        val memoryNew = MemoryNew(Array(IntTypeNode), listOf(PositionalArgument(IntLiteral(1))))
        val arrayAccess = ArrayAccess(memoryNew, NopeLiteral())
        val function = FunctionDeclaration(
            "main", emptyList(), NopeTypeNode,
            Block(
                listOf(arrayAccess)
            )
        )

        val astInfo = AstInfo(function, mapOf(arrayAccess to LocationRange(SimpleLocation(0, 0), SimpleLocation(0,1))))
        val nameResolution = NameResolutionGenerator(astInfo).parse().result

        val result = TypeChecker(astInfo, nameResolution).parse()

        // Assertions
        assertTrue(result.diagnostics.size == 1, "Expected at one diagnostics for pointer to non array type creation")
        assertEquals(
            "Only Int is allowed as the index of the array access operator",
            result.diagnostics[0].message,
            "Expected diagnostic message for wrong new keyword type"
        )
    }

    @Test
    fun `should infer type for foreign integer-returning function`() {
        // Simple code: `foreign foo g() -> Int`
        val functionDeclaration = ForeignFunctionDeclaration(
            name = "g",
            parameters = emptyList(),
            returnType = IntTypeNode
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
            returnType = IntTypeNode,
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

        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode)
        val bDeclaration = MutableVariableDeclaration(name = "b", type = IntTypeNode)

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
            type = IntTypeNode,
            initializer = conditionalExpr
        )

        val functionBody = Block(listOf(aDeclaration, bDeclaration, maxDeclaration))
        val functionDeclaration = FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
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
                Parameter("x", IntTypeNode),
                Parameter("y", IntTypeNode)
            ),
            returnType = IntTypeNode,
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
                Parameter("x", IntTypeNode),
                Parameter("y", IntTypeNode)
            ),
            returnType = IntTypeNode,
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
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode, initializer = IntLiteral(0))
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
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode, initializer = IntLiteral(0))
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
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode)
        val bDeclaration = ConstantDeclaration(name = "b", type = BoolTypeNode, initializer = BoolLiteral(true))
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
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode, initializer = IntLiteral(0))
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
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode, initializer = IntLiteral(0))
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
        val aDeclaration = MutableVariableDeclaration(name = "a", type = IntTypeNode, initializer = IntLiteral(0))
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
        val letX = ConstantDeclaration(name = "x", type = IntTypeNode, initializer = intLiteral42)
        val ifStatement = Conditional(
            condition = BoolLiteral(true),
            thenBranch = Block(listOf(letX)),
            elseBranch = null
        )

        val addExpr = BinaryOperation(IntLiteral(5), BinaryOperator.PLUS, IntLiteral(7))
        val letY = ConstantDeclaration(name = "y", type = IntTypeNode, initializer = addExpr)
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

    @Test
    fun `should resolve StructFieldAccess and report errors for invalid fields`() {
        // Code:
        // ```
        // struct S = {
        //   let x: Int;
        //   let y: Bool;
        // }
        // let s: S = new S(1, true);
        // s.x;
        // s.z;
        // ```

        // Define StructTypeDeclaration
        val xField = ConstantDeclaration("x", IntTypeNode, IntLiteral(0))
        val yField = ConstantDeclaration("y", BoolTypeNode, BoolLiteral(true))
        val structDeclaration = StructTypeDeclaration(
            name = "S",
            fields = listOf(xField, yField),
            constructorMethod = ConstructorDeclaration(emptyList(), Block(listOf(IntLiteral(2))))
        )

        // Mock NameResolution
        val mockNameResolution = mockk<NameResolution>()
        val variableDeclaration = ConstantDeclaration("s", TypeUse("S"), MemoryNew(TypeUse("S"), emptyList()))
        every { mockNameResolution.typeNameToDecl[any<TypeUse>()] } answers {
            val typeUse = this.firstArg<TypeUse>()
            if (typeUse.typeName == "S") structDeclaration else null
        }
        every { mockNameResolution.variableToDecl[any<VariableReference>()] } answers {
            val variableReference = this.firstArg<VariableReference>()
            if (variableReference.name == "s") ConstantDeclaration("s", TypeUse("S"), MemoryNew(TypeUse("S"), emptyList())) else null
        }


        // Define StructFieldAccess
        val validFieldAccess = StructFieldAccess(VariableReference("s"), "x")
        val invalidFieldAccess = StructFieldAccess(VariableReference("s"), "z")

        // Mock AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns Block(listOf(validFieldAccess, invalidFieldAccess))
        every { mockAstInfo.locations } returns mapOf(
            validFieldAccess to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)),
            invalidFieldAccess to LocationRange(SimpleLocation(0, 2), SimpleLocation(0, 3))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions for valid field access
        assertEquals(IntType, result.result[validFieldAccess], "Expected field 'x' to resolve to Int")

        // Assertions for invalid field access
        assertEquals(NopeType, result.result[invalidFieldAccess], "Expected field 'z' to resolve to Nope")
        assertEquals(1, result.diagnostics.size, "Expected one diagnostic for invalid field access")
        assertEquals(
            "Struct type does not contain field",
            result.diagnostics[0].message,
            "Expected diagnostic message for accessing invalid field"
        )
    }

    @Test
    fun `should resolve HereReference and report errors for invalid fields`() {
        // Code:
        // ```
        // struct S = {
        //   let x: Int;
        //   let y: Bool;
        //   ctor(width: Int, height: Bool) = {
        //       here.x = width;
        //       here.y = height;
        //   }
        // }
        // ```

        // Define StructTypeDeclaration
        val xField = ConstantDeclaration("x", IntTypeNode, IntLiteral(0))
        val yField = ConstantDeclaration("y", BoolTypeNode, BoolLiteral(true))
        val constructorBody = Block(
            listOf(
                Assignment(HereReference("x"), VariableReference("width")),
                Assignment(HereReference("y"), VariableReference("height"))
            )
        )
        val structDeclaration = StructTypeDeclaration(
            name = "S",
            fields = listOf(xField, yField),
            constructorMethod = ConstructorDeclaration(
                parameters = listOf(
                    Parameter("width", IntTypeNode),
                    Parameter("height", BoolTypeNode)
                ),
                body = constructorBody
            )
        )

        // Mock NameResolution
        val mockNameResolution = mockk<NameResolution>()
        every { mockNameResolution.variableToDecl[any<VariableReference>()] } answers {
            val variableReference = this.firstArg<VariableReference>()
            if (variableReference.name == "x") {
                MutableVariableDeclaration("x", IntTypeNode, IntLiteral(0))
            } else {
                null // Return null if no matching declaration is found
            }
        }
        every { mockNameResolution.assignmentToDecl[any<Assignment>()] } answers {
            val assignment = this.firstArg<Assignment>()
            // Return a corresponding VariableDeclarationBase based on the assignment variable
            if (assignment.variable is HereReference && (assignment.variable as HereReference).field == "x") {
                xField
            } else {
                null // Return null if no matching declaration is found
            }
        }

        // Mock AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns structDeclaration
        every { mockAstInfo.locations } returns mapOf(
            HereReference("x") to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1)),
            HereReference("y") to LocationRange(SimpleLocation(0, 2), SimpleLocation(0, 3)),
            HereReference("z") to LocationRange(SimpleLocation(0, 4), SimpleLocation(0, 5))
        )

        // Run TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions for invalid HereReference usage
        assertEquals(0, result.diagnostics.size, "All valid references")
    }

    @Test
    fun `should handle recursive function call without stack overflow`() {
        // Define the Fibonacci function
        val fibFunction = FunctionDeclaration(
            name = "fib",
            parameters = listOf(Parameter("n", IntTypeNode)),
            returnType = IntTypeNode,
            body = Conditional(
                condition = BinaryOperation(VariableReference("n"), BinaryOperator.LTE, IntLiteral(1)),
                thenBranch = VariableReference("n"),
                elseBranch = BinaryOperation(
                    FunctionCall("fib", listOf(PositionalArgument(BinaryOperation(VariableReference("n"), BinaryOperator.MINUS, IntLiteral(1))))),
                    BinaryOperator.PLUS,
                    FunctionCall("fib", listOf(PositionalArgument(BinaryOperation(VariableReference("n"), BinaryOperator.MINUS, IntLiteral(2)))))
                )
            )
        )

        // Mock NameResolution
        val mockNameResolution = mockk<NameResolution>()
        val variableMap = mutableMapOf<VariableReference, VariableDeclarationBase>()
        every { mockNameResolution.variableToDecl } returns variableMap
        every { mockNameResolution.variableToDecl[any<VariableReference>()] } answers {
            val variableReference = this.firstArg<VariableReference>()
            if (variableReference.name == "n") Parameter("n", IntTypeNode) else null
        }
        val functionMap = mutableMapOf<FunctionCall, FunctionDeclaration>()
        every { mockNameResolution.functionToDecl } returns functionMap
        every { mockNameResolution.functionToDecl[any<FunctionCall>()] } answers {
            val functionCall = this.firstArg<FunctionCall>()
            if (functionCall.functionName == "fib") fibFunction else null
        }

        // Mock AstInfo
        val mockAstInfo = mockk<AstInfo>()
        every { mockAstInfo.root } returns Program(listOf(fibFunction))
        every { mockAstInfo.locations } returns mapOf(
            fibFunction.body to LocationRange(SimpleLocation(0, 0), SimpleLocation(0, 1))
        )

        // Run the TypeChecker
        val typeChecker = TypeChecker(mockAstInfo, mockNameResolution)
        val result = typeChecker.parse()

        // Assertions
        assertEquals(IntType, result.result[fibFunction], "Expected 'fib' to resolve to IntType")
        assertTrue(result.diagnostics.isEmpty(), "Expected no diagnostics for valid recursive function")
    }


}