package org.exeval.ast.valid.conditionals

import org.exeval.ast.*

val CONDITIONALS_CONDITIONALS_INSIDE_LOOP_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "greaterThan",
            parameters = listOf(
                Parameter(name = "a", type = IntType),
                Parameter(name = "b", type = IntType)
            ),
            returnType = BoolType,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("a"),
                            operator = BinaryOperator.GT,
                            right = VariableReference("b")
                        ),
                        thenBranch = BoolLiteral(true),
                        elseBranch = BoolLiteral(false)
                    )
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = FunctionCall(
                            functionName = "greaterThan",
                            arguments = listOf(
                                PositionalArgument(IntLiteral(10)),
                                PositionalArgument(IntLiteral(5))
                            )
                        ),
                        thenBranch = IntLiteral(1),
                        elseBranch = IntLiteral(0)
                    )
                )
            )
        )
    )
)
