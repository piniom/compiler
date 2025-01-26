package org.exeval.ast.valid.separator

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val SEPARATOR_DELIMITED_STATEMENTS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    FunctionDeclaration(
                        name = "function",
                        parameters = listOf(
                            Parameter(name = "a", type = BoolTypeNode),
                            Parameter(name = "b", type = BoolTypeNode)
                        ),
                        returnType = BoolTypeNode,
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
                            Parameter(name = "a", type = BoolTypeNode),
                            Parameter(name = "b", type = BoolTypeNode)
                        ),
                        returnType = BoolTypeNode,
                        body = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.OR,
                            right = VariableReference(name = "b")
                        )
                    ),
                    ConstantDeclaration(
                        name = "a",
                        type = BoolTypeNode,
                        initializer = BoolLiteral(value = true)
                    ),
                    ConstantDeclaration(
                        name = "b",
                        type = BoolTypeNode,
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
                    ),
                    IntLiteral(0),
                )
            )
        )
    )
)