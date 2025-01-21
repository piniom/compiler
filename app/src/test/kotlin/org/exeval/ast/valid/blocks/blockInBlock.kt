package org.exeval.ast.valid.blocks

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val BLOCKS_BLOCK_IN_BLOCK_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "a",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "b",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "c",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "d",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    Block(
                        expressions = listOf(
                            MutableVariableDeclaration(
                                name = "i",
                                type = IntTypeNode,
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
                                            variable = VariableReference("i"),
                                            value = VariableReference("b")
                                        )
                                    )
                                )
                            ),
                            Assignment(
                                variable = VariableReference("i"),
                                value = BinaryOperation(
                                    left = VariableReference("i"),
                                    operator = BinaryOperator.PLUS,
                                    right = IntLiteral(1)
                                )
                            ),
                            Assignment(
                                variable = VariableReference("c"),
                                value = VariableReference("i")
                            ),
                            Block(
                                expressions = listOf(
                                    MutableVariableDeclaration(
                                        name = "j",
                                        type = IntTypeNode,
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
                                                    variable = VariableReference("j"),
                                                    value = VariableReference("b")
                                                )
                                            )
                                        )
                                    ),
                                    Assignment(
                                        variable = VariableReference("i"),
                                        value = BinaryOperation(
                                            left = VariableReference("j"),
                                            operator = BinaryOperator.PLUS,
                                            right = IntLiteral(1)
                                        )
                                    ),
                                    Assignment(
                                        variable = VariableReference("d"),
                                        value = VariableReference("i")
                                    )
                                )
                            )
                        )
                    ),
                    IntLiteral(0)
                )
            )
        )
    )
)