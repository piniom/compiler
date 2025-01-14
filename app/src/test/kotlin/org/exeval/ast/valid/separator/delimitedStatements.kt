package org.exeval.ast.valid.separator

import org.exeval.ast.*

val SEPARATOR_DELIMITED_STATEMENTS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    FunctionDeclaration(
                        name = "function",
                        parameters = listOf(
                            Parameter(name = "a", type = BoolType),
                            Parameter(name = "b", type = BoolType)
                        ),
                        returnType = BoolType,
                        body = Block(
                            expressions = listOf(
                                BinaryOperation(
                                    left = VariableReference(name = "a"),
                                    operator = BinaryOperator.AND,
                                    right = VariableReference(name = "b")
                                )
                            )
                        )
                    ),
                    FunctionDeclaration(
                        name = "function2",
                        parameters = listOf(
                            Parameter(name = "a", type = BoolType),
                            Parameter(name = "b", type = BoolType)
                        ),
                        returnType = BoolType,
                        body = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.OR,
                            right = VariableReference(name = "b")
                        )
                    ),
                    ConstantDeclaration(
                        name = "a",
                        type = BoolType,
                        initializer = BoolLiteral(value = true)
                    ),
                    ConstantDeclaration(
                        name = "b",
                        type = BoolType,
                        initializer = BoolLiteral(value = false)
                    ),
                    Block(
                        expressions = listOf(
                            Block(
                                expressions = listOf(
                                    BinaryOperation(
                                        left = VariableReference(name = "a"),
                                        operator = BinaryOperator.AND,
                                        right = VariableReference(name = "b")
                                    )
                                )
                            ),
                            Block(
                                expressions = listOf(
                                    BinaryOperation(
                                        left = VariableReference(name = "a"),
                                        operator = BinaryOperator.OR,
                                        right = VariableReference(name = "b")
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