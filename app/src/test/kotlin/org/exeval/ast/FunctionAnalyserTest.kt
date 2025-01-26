package org.exeval.ast

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FunctionAnalyserTest {

    // Helper function to create mock Type objects
    private fun mockType(): TypeNode = mockk()

    @Test
    fun `test function analysis with simple functions`() {
        // Create a simple AST with two functions
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = BinaryOperation(
                        left = VariableReference("x"),
                        operator = BinaryOperator.PLUS,
                        right = IntLiteral(1)
                    )
                ),
                FunctionDeclaration(
                    name = "bar",
                    parameters = listOf(Parameter("y", intType)),
                    returnType = intType,
                    body = FunctionCall(
                        functionName = "foo",
                        arguments = listOf(PositionalArgument(VariableReference("y")))
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap()) // Mock location map as it's not important here

        // Create the analyzer instance
        val analyser = FunctionAnalyser()

        // Perform analysis
        val analysisResult = analyser.analyseFunctions(astInfo)

        // Test Call Graph
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[1])) // "bar" should call "foo"
        assertTrue(callGraph[program.functions[1]]?.contains(program.functions[0]) == true)

        // Test Static Parents
        val staticParents = analysisResult.staticParents
        assertEquals(staticParents[program.functions[0]], null) // "foo" has no parent, it's global
        assertEquals(staticParents[program.functions[1]], null) // "bar" is also global here (no parent)

        // Test Variable Map
        val variableMap = analysisResult.variableMap
        assertTrue(variableMap.containsKey(program.functions[0].parameters[0])) // "foo" has parameter "x"
        assertTrue(variableMap.containsKey(program.functions[1].parameters[0])) // "bar" has parameter "y"

        // Test Nested Variable Usage
        val isUsedInNested = analysisResult.isUsedInNested
        assertFalse(isUsedInNested[program.functions[0].parameters[0]] == true) // "x" is used directly in "foo"
        assertFalse(isUsedInNested[program.functions[1].parameters[0]] == true) // "y" is used directly in "bar"
    }

    @Test
    fun `test function analysis with nested function calls`() {
        // Create a program where one function calls another
        val intType: TypeNode = mockType()

        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = Block(
                        expressions = listOf(
                            FunctionDeclaration(
                                name = "bar",
                                parameters = listOf(Parameter("y", intType)),
                                returnType = intType,
                                body = BinaryOperation(
                                    left = VariableReference("y"),
                                    operator = BinaryOperator.PLUS,
                                    right = IntLiteral(2)
                                )
                            ),
                            FunctionCall(
                                functionName = "bar",
                                arguments = listOf(PositionalArgument(VariableReference("x")))
                            )
                        )
                    )
                ), FunctionDeclaration(
                    name = "bar",
                    parameters = listOf(Parameter("y", intType)),
                    returnType = intType,
                    body = BinaryOperation(
                        left = VariableReference("y"),
                        operator = BinaryOperator.PLUS,
                        right = IntLiteral(2)
                    )
                )
            ),
            structures = listOf(),
        )

        val body = (program.functions[0] as FunctionDeclaration).body as Block

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Test Call Graph
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "foo"
        assertTrue(!callGraph[program.functions[0]]!!.contains(program.functions[1]) == true) // "foo" does no call global "bar"
        assertTrue(callGraph[program.functions[0]]!!.contains(body.expressions[0]) == true) // "foo" calls local "bar" function
        assertTrue(callGraph.containsKey(program.functions[1])) // "bar" should not call at all

        // Test Static Parents
        val staticParents = analysisResult.staticParents
        assertNull(staticParents[program.functions[0]]) // "foo" is global
        assertEquals(staticParents[body.expressions[0]], program.functions[0]) // local "bar" is defined inside "foo"
        assertNull(staticParents[program.functions[1]]) // "bar" is global

        // Test Variable Map
        val variableMap = analysisResult.variableMap
        assertTrue(variableMap.containsKey(program.functions[0].parameters[0])) // "foo" has parameter "x"
        assertTrue(variableMap.containsKey(program.functions[1].parameters[0])) // "bar" has parameter "y"
    }

    @Test
    fun `test function analysis with empty program`() {
        // Test case with no functions
        val program = Program(
            functions = emptyList(),
             structures = listOf(),
        )
        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure the analysis result is empty for all components
        assertTrue(analysisResult.callGraph.isEmpty())
        assertTrue(analysisResult.staticParents.isEmpty())
        assertTrue(analysisResult.variableMap.isEmpty())
        assertTrue(analysisResult.isUsedInNested.isEmpty())
    }

    @Test
    fun `test function analysis with a function having no calls`() {
        // A program with a function that doesn't call anything
        val intType: TypeNode = mockType()
        val fooDeclaration = FunctionDeclaration(
            name = "foo",
            parameters = listOf(Parameter("x", intType)),
            returnType = intType,
            body = BinaryOperation(
                left = VariableReference("x"),
                operator = BinaryOperator.PLUS,
                right = IntLiteral(1)
            )
        )
        val program = Program(
            functions = listOf(
                fooDeclaration
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure there are no function calls
        analysisResult.callGraph.forEach { (key, value) ->
            assertTrue(value.isEmpty())
        }
    }

    @Test
    fun `test function analysis with function calling itself (recursion)`() {
        // A function calling itself (recursion)
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "factorial",
                    parameters = listOf(Parameter("n", intType)),
                    returnType = intType,
                    body = Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("n"),
                            operator = BinaryOperator.EQ,
                            right = IntLiteral(0)
                        ),
                        thenBranch = IntLiteral(1),
                        elseBranch = FunctionCall(
                            functionName = "factorial",
                            arguments = listOf(
                                PositionalArgument(
                                    BinaryOperation(
                                        left = VariableReference("n"),
                                        operator = BinaryOperator.MINUS,
                                        right = IntLiteral(1)
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Test the recursion
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "factorial" should be in the call graph
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[0]) == true) // "factorial" calls itself
    }

    @Test
    fun `test function analysis with loop and function calls`() {
        // A function that contains a loop, which calls another function inside the loop
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "processData",
                    parameters = listOf(),
                    returnType = intType,
                    body = Loop(
                        identifier = "loop1",
                        body = Block(
                            expressions = listOf(
                                FunctionCall(
                                    functionName = "foo",
                                    arguments = listOf(PositionalArgument(IntLiteral(10)))
                                )
                            )
                        )
                    )
                ),
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = IntLiteral(42)
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure the loop is analyzed and that "processData" calls "foo"
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "processData"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[1]) == true) // "processData" calls "foo"
    }

    @Test
    fun `test function analysis with break statement inside loop`() {
        // A function with a loop and a break statement inside
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "processData",
                    parameters = listOf(),
                    returnType = intType,
                    body = Loop(
                        identifier = "loop1",
                        body = Block(
                            expressions = listOf(
                                FunctionCall(
                                    functionName = "foo",
                                    arguments = listOf(PositionalArgument(IntLiteral(10)))
                                ),
                                Break(identifier = "loop1")
                            )
                        )
                    )
                ),
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = IntLiteral(42)
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure the break statement is handled and that "processData" calls "foo"
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "processData"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[1]) == true) // "processData" calls "foo"
    }

    @Test
    fun `test function analysis with conditional and multiple branches`() {
        // A function with a conditional statement that has multiple branches
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "checkValue",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("x"),
                            operator = BinaryOperator.GT,
                            right = IntLiteral(0)
                        ),
                        thenBranch = FunctionCall(
                            functionName = "positive",
                            arguments = listOf(PositionalArgument(VariableReference("x")))
                        ),
                        elseBranch = FunctionCall(
                            functionName = "negative",
                            arguments = listOf(PositionalArgument(VariableReference("x")))
                        )
                    )
                ),
                FunctionDeclaration(
                    name = "positive",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = IntLiteral(1)
                ),
                FunctionDeclaration(
                    name = "negative",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = IntLiteral(-1)
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure that "checkValue" calls both "positive" and "negative" functions depending on the condition
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "checkValue"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[1]) == true) // "checkValue" calls "positive"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[2]) == true) // "checkValue" calls "negative"
    }

    @Test
    fun `test function analysis with unused variables`() {
        // A function with a declared variable that is not used in the body
        val intType: TypeNode = mockType()
        val variable = MutableVariableDeclaration(name = "unusedVar", type = intType)
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(),
                    returnType = intType,
                    body = Block(
                        expressions = listOf(
                            variable,
                            IntLiteral(42)
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure the unused variable is not marked as used
        assertTrue(analysisResult.variableMap.containsKey(variable)) // "unusedVar"
        assertEquals(analysisResult.variableMap[variable], program.functions[0])
        assertTrue(analysisResult.isUsedInNested[variable] == false) // Variable not nested
    }

    @Test
    fun `test function analysis with complex nested structures`() {
        // A function with deeply nested expressions
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "calculate",
                    parameters = listOf(),
                    returnType = intType,
                    body = Conditional(
                        condition = BinaryOperation(
                            left = IntLiteral(5),
                            operator = BinaryOperator.LT,
                            right = IntLiteral(10)
                        ),
                        thenBranch = FunctionCall(
                            functionName = "foo",
                            arguments = listOf(PositionalArgument(IntLiteral(2)))
                        ),
                        elseBranch = FunctionCall(
                            functionName = "bar",
                            arguments = listOf(PositionalArgument(IntLiteral(3)))
                        )
                    )
                ),
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = IntLiteral(42)
                ),
                FunctionDeclaration(
                    name = "bar",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = IntLiteral(24)
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure the deep nested structure is analyzed correctly
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "calculate"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[1]) == true) // "calculate" calls "foo"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[2]) == true) // "calculate" calls "bar"
    }

    @Test
    fun `test function analysis with nested function declarations`() {
        // A function with a nested function declaration
        val intType: TypeNode = mockType()
        val innerFunc = FunctionDeclaration(
            name = "innerFunction",
            parameters = listOf(),
            returnType = intType,
            body = IntLiteral(42)
        )
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "outerFunction",
                    parameters = listOf(),
                    returnType = intType,
                    body = Block(
                        expressions = listOf(
                            innerFunc,
                            FunctionCall(
                                functionName = "innerFunction",
                                arguments = listOf()
                            )
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure that the inner function is analyzed and called within the outer function
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "outerFunction"
        assertTrue(callGraph[program.functions[0]]!!.contains(innerFunc)) // "outerFunction" calls "innerFunction"
    }

    @Test
    fun `test function analysis with recursive function call`() {
        // A recursive function that calls itself
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "factorial",
                    parameters = listOf(Parameter("n", intType)),
                    returnType = intType,
                    body = Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("n"),
                            operator = BinaryOperator.EQ,
                            right = IntLiteral(0)
                        ),
                        thenBranch = IntLiteral(1),
                        elseBranch = BinaryOperation(
                            left = VariableReference("n"),
                            operator = BinaryOperator.MULTIPLY,
                            right = FunctionCall(
                                functionName = "factorial",
                                arguments = listOf(
                                    PositionalArgument(
                                        BinaryOperation(
                                            left = VariableReference("n"),
                                            operator = BinaryOperator.MINUS,
                                            right = IntLiteral(1)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure that the recursive function "factorial" is correctly analyzed and that it calls itself
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "factorial"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[0]) == true) // "factorial" calls itself
    }

    @Test
    fun `test function analysis with multiple nested recursive calls`() {
        // A function that calls itself indirectly via another function
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("n", intType)),
                    returnType = intType,
                    body = Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("n"),
                            operator = BinaryOperator.EQ,
                            right = IntLiteral(0)
                        ),
                        thenBranch = IntLiteral(1),
                        elseBranch = FunctionCall(
                            functionName = "bar",
                            arguments = listOf(PositionalArgument(VariableReference("n")))
                        )
                    )
                ),
                FunctionDeclaration(
                    name = "bar",
                    parameters = listOf(Parameter("n", intType)),
                    returnType = intType,
                    body = FunctionCall(
                        functionName = "foo",
                        arguments = listOf(
                            PositionalArgument(
                                BinaryOperation(
                                    left = VariableReference("n"),
                                    operator = BinaryOperator.MINUS,
                                    right = IntLiteral(1)
                                )
                            )
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure that the functions foo and bar are calling each other in a recursive manner
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "foo"
        assertTrue(callGraph.containsKey(program.functions[1])) // "bar"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[1]) == true) // "foo" calls "bar"
        assertTrue(callGraph[program.functions[1]]?.contains(program.functions[0]) == true) // "bar" calls "foo"
    }

    @Test
    fun `test function analysis with nested recursive calls and variable usage`() {
        // Recursive function with nested calls and variable usage
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "fibonacci",
                    parameters = listOf(Parameter("n", intType)),
                    returnType = intType,
                    body = Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("n"),
                            operator = BinaryOperator.LT,
                            right = IntLiteral(2)
                        ),
                        thenBranch = IntLiteral(1),
                        elseBranch = BinaryOperation(
                            left = FunctionCall(
                                functionName = "fibonacci",
                                arguments = listOf(
                                    PositionalArgument(
                                        BinaryOperation(
                                            left = VariableReference("n"),
                                            operator = BinaryOperator.MINUS,
                                            right = IntLiteral(1)
                                        )
                                    )
                                )
                            ),
                            operator = BinaryOperator.PLUS,
                            right = FunctionCall(
                                functionName = "fibonacci",
                                arguments = listOf(
                                    PositionalArgument(
                                        BinaryOperation(
                                            left = VariableReference("n"),
                                            operator = BinaryOperator.MINUS,
                                            right = IntLiteral(2)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure that the recursive function "fibonacci" is correctly analyzed and calls itself with the right parameters
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "fibonacci"
        assertTrue(callGraph[program.functions[0]]?.contains(program.functions[0]) == true) // "fibonacci" calls itself
    }

    @Test
    fun `test function analysis with deeply nested function declarations`() {
        // A deeply nested function that has another function inside it
        val intType: TypeNode = mockType()
        val innerFunc = FunctionDeclaration(
            name = "innerFunction",
            parameters = listOf(),
            returnType = intType,
            body = IntLiteral(42)
        )
        val middleFunction = FunctionDeclaration(
            name = "middleFunction",
            parameters = listOf(),
            returnType = intType,
            body = Block(
                expressions = listOf(
                    innerFunc,
                    FunctionCall(
                        functionName = "innerFunction",
                        arguments = listOf()
                    )
                )
            )
        )
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "outerFunction",
                    parameters = listOf(),
                    returnType = intType,
                    body = Block(
                        expressions = listOf(
                            middleFunction,
                            FunctionCall(
                                functionName = "middleFunction",
                                arguments = listOf()
                            )
                        )
                    )
                )
            ),
            structures = listOf(),
        )

        val astInfo = AstInfo(program, locations = emptyMap())

        val analyser = FunctionAnalyser()

        val analysisResult = analyser.analyseFunctions(astInfo)

        // Ensure that all nested functions are correctly analyzed and called
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[0])) // "outerFunction"
        assertTrue(callGraph[program.functions[0]]!!.contains(middleFunction)) // "outerFunction" calls "middleFunction"
        assertTrue(callGraph[middleFunction]!!.contains(innerFunc)) // "middleFunction" calls "innerFunction"
    }

    @Test
    fun `test function analysis with simple constructor`() {
        // Create a simple AST with two functions
        val intType: TypeNode = mockType()
        val program = Program(
            functions = listOf(
                FunctionDeclaration(
                    name = "foo",
                    parameters = listOf(Parameter("x", intType)),
                    returnType = intType,
                    body = BinaryOperation(
                        left = VariableReference("x"),
                        operator = BinaryOperator.PLUS,
                        right = IntLiteral(1)
                    )
                ),
                FunctionDeclaration(
                    name = "bar",
                    parameters = listOf(Parameter("y", intType)),
                    returnType = intType,
                    body = FunctionCall(
                        functionName = "foo",
                        arguments = listOf(PositionalArgument(VariableReference("y")))
                    )
                )
            ),
            structures = listOf(
                StructTypeDeclaration(
                    "struct", listOf(ConstantDeclaration("field", IntTypeNode, IntLiteral(1))),
                    ConstructorDeclaration(
                        listOf(Parameter("arg", IntTypeNode)), Block(
                            listOf(
                                Assignment(StructFieldAccess(HereReference(null), "field"), VariableReference("arg")),
                                FunctionCall(
                                    functionName = "foo",
                                    arguments = listOf(PositionalArgument(VariableReference("y")))
                                )
                            )
                        )
                    )
                )
            )
        )

        val astInfo = AstInfo(program, locations = emptyMap()) // Mock location map as it's not important here

        // Create the analyzer instance
        val analyser = FunctionAnalyser()

        // Perform analysis
        val analysisResult = analyser.analyseFunctions(astInfo)

        // Test Call Graph
        val callGraph = analysisResult.callGraph
        assertTrue(callGraph.containsKey(program.functions[1])) // "bar" should call "foo"
        assertTrue(callGraph[program.functions[1]]?.contains(program.functions[0]) == true)
        assertTrue(callGraph.containsKey(program.structures[0].constructorMethod)) // "constructor" should call "foo"
        assertTrue(callGraph[program.structures[0].constructorMethod]?.contains(program.functions[0]) == true)

        // Test Static Parents
        val staticParents = analysisResult.staticParents
        assertEquals(staticParents[program.functions[0]], null) // "foo" has no parent, it's global
        assertEquals(staticParents[program.functions[1]], null) // "bar" is also global here (no parent)

        // Test Variable Map
        val variableMap = analysisResult.variableMap
        assertTrue(variableMap.containsKey(program.functions[0].parameters[0])) // "foo" has parameter "x"
        assertTrue(variableMap.containsKey(program.functions[1].parameters[0])) // "bar" has parameter "y"

        // Test Nested Variable Usage
        val isUsedInNested = analysisResult.isUsedInNested
        assertFalse(isUsedInNested[program.functions[0].parameters[0]] == true) // "x" is used directly in "foo"
        assertFalse(isUsedInNested[program.functions[1].parameters[0]] == true) // "y" is used directly in "bar"
    }

}
