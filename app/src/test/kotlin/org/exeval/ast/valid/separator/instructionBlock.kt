package org.exeval.ast.valid.separator

import org.exeval.ast.*

val SEPARATOR_INSTRUCTION_BLOCK_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    Block(
                        expressions = listOf(
                            BinaryOperation(
                                left = IntLiteral(value = 2),
                                operator = BinaryOperator.MULTIPLY,
                                right = IntLiteral(value = 4)
                            ),
                            BinaryOperation(
                                left = IntLiteral(value = 1),
                                operator = BinaryOperator.PLUS,
                                right = IntLiteral(value = 3)
                            ),
                            BinaryOperation(
                                left = IntLiteral(value = 7),
                                operator = BinaryOperator.DIVIDE,
                                right = IntLiteral(value = 7)
                            )
                        )
                    )
                )
            )
        )
    )
)