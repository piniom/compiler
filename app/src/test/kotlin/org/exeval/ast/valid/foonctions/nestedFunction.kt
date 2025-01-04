package org.exeval.ast.valid.foonctions

import org.exeval.ast.*

val FOONCTIONS_NESTED_FUNTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "ext",
                        type = IntType,
                        initializer = IntLiteral(5)
                    ),
                    FunctionDeclaration(
                        name = "nested",
                        parameters = listOf(
                            Parameter(name = "a", type = IntType)
                        ),
                        returnType = IntType,
                        body = Block(
                            expressions = listOf(
                                Conditional(
                                    condition = BinaryOperation(
                                        left = VariableReference("ext"),
                                        operator = BinaryOperator.GT,
                                        right = IntLiteral(3)
                                    ),
                                    thenBranch = Block(
                                        expressions = listOf(
                                            BinaryOperation(
                                                left = VariableReference("a"),
                                                operator = BinaryOperator.PLUS,
                                                right = VariableReference("ext")
                                            )
                                        )
                                    ),
                                    elseBranch = Block(
                                        expressions = listOf(
                                            BinaryOperation(
                                                left = VariableReference("a"),
                                                operator = BinaryOperator.MINUS,
                                                right = VariableReference("ext")
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    Assignment(
                        variable = VariableReference("ext"),
                        value = IntLiteral(2)
                    ),
                    FunctionCall(
                        functionName = "nested",
                        arguments = listOf(
                            PositionalArgument(IntLiteral(3))
                        )
                    )
                )
            )
        )
    )
)