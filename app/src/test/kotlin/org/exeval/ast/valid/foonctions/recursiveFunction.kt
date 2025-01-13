package org.exeval.ast.valid.foonctions

import org.exeval.ast.*
import org.exeval.ast.Int

val FOONCTIONS_RECURSSIVE_FUNCTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "fib",
            parameters = listOf(
                Parameter(name = "n", type = Int)
            ),
            returnType = Int,
            body = Block (
                expressions = listOf(
                    Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("n"),
                            operator = BinaryOperator.LT,
                            right = IntLiteral(3)
                        ),
                        thenBranch = Block(
                            expressions = listOf(
                                IntLiteral(1)
                            )
                        ),
                        elseBranch =  Block (
                            expressions = listOf(
                                BinaryOperation(
                                    left = FunctionCall(
                                        functionName = "fib",
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
                                        functionName = "fib",
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
                    )
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    FunctionCall(
                        functionName = "fib",
                        arguments = listOf(
                            PositionalArgument(IntLiteral(5))
                        )
                    )
                )
            )
        )
    )
)