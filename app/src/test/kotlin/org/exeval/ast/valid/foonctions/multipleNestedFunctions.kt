package org.exeval.ast.valid.foonctions

import org.exeval.ast.*
import org.exeval.ast.Int

val FOONCTIONS_MULTIPLE_NESTED_FUNCTIONS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "g",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "a",
                        type = Int,
                        initializer = IntLiteral(10)
                    ),
                    FunctionDeclaration(
                        name = "f1",
                        parameters = emptyList(),
                        returnType = Int,
                        body = BinaryOperation(
                            left = VariableReference("a"),
                            operator = BinaryOperator.PLUS,
                            right = IntLiteral(1)
                        )
                    ),
                    FunctionDeclaration(
                        name = "f2",
                        parameters = emptyList(),
                        returnType = Int,
                        body = BinaryOperation(
                            left = VariableReference("a"),
                            operator = BinaryOperator.PLUS,
                            right = IntLiteral(2)
                        )
                    ),
                    FunctionDeclaration(
                        name = "f3",
                        parameters = listOf(
                            Parameter(name = "a", type = Int)
                        ),
                        returnType = Int,
                        body = BinaryOperation(
                            left = VariableReference("a"),
                            operator = BinaryOperator.PLUS,
                            right = IntLiteral(3)
                        )
                    ),
                    FunctionDeclaration(
                        name = "f4",
                        parameters = emptyList(),
                        returnType = Int,
                        body = Block(
                            expressions = listOf(
                                ConstantDeclaration(
                                    name = "a",
                                    type = Int,
                                    initializer = IntLiteral(1)
                                ),
                                BinaryOperation(
                                    left = VariableReference("a"),
                                    operator = BinaryOperator.PLUS,
                                    right = IntLiteral(4)
                                )
                            )
                        )
                    ),
                    BinaryOperation(
                        left = FunctionCall(
                            functionName = "f1",
                            arguments = emptyList()
                        ),
                        operator = BinaryOperator.PLUS,
                        right = BinaryOperation(
                            left = FunctionCall(
                                functionName = "f2",
                                arguments = emptyList()
                            ),
                            operator = BinaryOperator.PLUS,
                            right = BinaryOperation(
                                left = FunctionCall(
                                    functionName = "f3",
                                    arguments = listOf(
                                        PositionalArgument(IntLiteral(5))
                                    )
                                ),
                                operator = BinaryOperator.PLUS,
                                right = FunctionCall(
                                    functionName = "f4",
                                    arguments = emptyList()
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
                        functionName = "g",
                        arguments = emptyList()
                    )
                )
            )
        )
    )
)