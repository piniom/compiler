package org.exeval.ast.valid.loops

import org.exeval.ast.*

val LOOPS_BREAK_WITH_LOOP_VALUE_IN_BREAK_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    Loop(
                        identifier = null,
                        body = Block(
                            expressions = listOf(
                                Break(
                                    identifier = null,
                                    expression = BinaryOperation(
                                        left = BinaryOperation(
                                            left = IntLiteral(value = 7),
                                            operator = BinaryOperator.PLUS,
                                            right = IntLiteral(value = 19)
                                        ),
                                        operator = BinaryOperator.MULTIPLY,
                                        right = Loop(
                                            identifier = null,
                                            body = Block(
                                                expressions = listOf(
                                                    Break(
                                                        identifier = null,
                                                        expression = IntLiteral(value = 2)
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
)