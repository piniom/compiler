package org.exeval.ast.valid.blocks

import org.exeval.ast.*

val BLOCKS_BLOCK_IN_BLOCK_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "a",
                        type = IntType,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "b",
                        type = IntType,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "c",
                        type = IntType,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "d",
                        type = IntType,
                        initializer = null
                    ),
                    Block(
                        expressions = listOf(
                            MutableVariableDeclaration(
                                name = "i",
                                type = IntType,
                                initializer = VariableReference("a")
                            ),
                            Conditional(
                                condition = BinaryOperation(
                                    left = VariableReference("b"),
                                    operator = BinaryOperator.GT,
                                    right = VariableReference("i")
                                ),
                                thenBranch = Block(
                                    expressions = listOf(
                                        Assignment(
                                            variable = "i",
                                            value = VariableReference("b")
                                        )
                                    )
                                )
                            ),
                            Assignment(
                                variable = "i",
                                value = BinaryOperation(
                                    left = VariableReference("i"),
                                    operator = BinaryOperator.PLUS,
                                    right = IntLiteral(1)
                                )
                            ),
                            Assignment(
                                variable = "c",
                                value = VariableReference("i")
                            ),
                            Block(
                                expressions = listOf(
                                    MutableVariableDeclaration(
                                        name = "j",
                                        type = IntType,
                                        initializer = VariableReference("a")
                                    ),
                                    Conditional(
                                        condition = BinaryOperation(
                                            left = VariableReference("b"),
                                            operator = BinaryOperator.GT,
                                            right = VariableReference("j")
                                        ),
                                        thenBranch = Block(
                                            expressions = listOf(
                                                Assignment(
                                                    variable = "j",
                                                    value = VariableReference("b")
                                                )
                                            )
                                        )
                                    ),
                                    Assignment(
                                        variable = "i",
                                        value = BinaryOperation(
                                            left = VariableReference("j"),
                                            operator = BinaryOperator.PLUS,
                                            right = IntLiteral(1)
                                        )
                                    ),
                                    Assignment(
                                        variable = "d",
                                        value = VariableReference("i")
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