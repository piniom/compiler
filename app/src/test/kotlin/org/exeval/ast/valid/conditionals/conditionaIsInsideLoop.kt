package org.exeval.ast.valid.conditionals

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val CONDITIONALS_CONDITIONALS_INSIDE_LOOP_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "x",
                        type = IntTypeNode,
                        initializer = IntLiteral(0)
                    ),
                    Loop(
                        identifier = null,
                        body = Block(
                            expressions = listOf(
                                Conditional(
                                    condition = BinaryOperation(
                                        left = VariableReference("x"),
                                        operator = BinaryOperator.GTE,
                                        right = IntLiteral(5)
                                    ),
                                    thenBranch = Block(
                                        expressions = listOf(
                                            Break(
                                                identifier = null,
                                                expression = VariableReference("x")
                                            ),
                                            NopeLiteral(),
                                        )
                                    ),
                                    elseBranch = Block(
                                        expressions = listOf(
                                            Assignment(
                                                variable = VariableReference("x"),
                                                value = BinaryOperation(
                                                    left = VariableReference("x"),
                                                    operator = BinaryOperator.PLUS,
                                                    right = IntLiteral(1)
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
    )
)
