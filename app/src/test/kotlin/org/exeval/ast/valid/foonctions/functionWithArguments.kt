package org.exeval.ast.valid.foonctions

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val FOONCTIONS_FUNCTION_WITH_ARGUMENTS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "f",
            parameters = listOf(
                Parameter(name = "a", type = IntTypeNode),
                Parameter(name = "b", type = IntTypeNode)
            ),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    BinaryOperation(
                        left = VariableReference("a"),
                        operator = BinaryOperator.PLUS,
                        right = VariableReference("b")
                    )
                )
            )
        ),
        FunctionDeclaration(
            name = "g",
            parameters = listOf(
                Parameter(name = "a", type = IntTypeNode),
                Parameter(name = "b", type = IntTypeNode)
            ),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    BinaryOperation(
                        left = VariableReference("a"),
                        operator = BinaryOperator.MINUS,
                        right = VariableReference("b")
                    )
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    BinaryOperation(
                        left = FunctionCall(
                            functionName = "f",
                            arguments = listOf(
                                PositionalArgument(IntLiteral(1)),
                                PositionalArgument(IntLiteral(2))
                            )
                        ),
                        operator = BinaryOperator.PLUS,
                        right = FunctionCall(
                            functionName = "g",
                            arguments = listOf(
                                NamedArgument(name = "b", expression = IntLiteral(1)),
                                NamedArgument(name = "a", expression = IntLiteral(2))
                            )
                        )
                    )
                )
            )
        )
    )
)