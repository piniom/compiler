package org.exeval.ast.valid.blocks

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val BLOCK_MAX_AST = Program(
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
                    ConstantDeclaration(
                        name = "max",
                        type = IntTypeNode,
                        initializer = Block(
                            expressions = listOf(
                                Conditional(
                                    condition = BinaryOperation(
                                        left = VariableReference("a"),
                                        operator = BinaryOperator.GT,
                                        right = VariableReference("b")
                                    ),
                                    thenBranch = Block(
                                        expressions = listOf(
                                            VariableReference("a")
                                        )
                                    ),
                                    elseBranch = Block(
                                        expressions = listOf(
                                            VariableReference("b")
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
)