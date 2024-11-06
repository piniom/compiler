package org.exeval.ast.valid.conditionals

import org.exeval.ast.*

val CONDITIONALS_IF_WITH_ELSE_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "y",
                        type = IntType,
                        initializer = Conditional(
                            condition = BinaryOperation(
                                left = IntLiteral(5),
                                operator = BinaryOperator.GT,
                                right = IntLiteral(3)
                            ),
                            thenBranch = Block(
                                expressions = listOf(
                                    ConstantDeclaration(
                                        name = "a",
                                        type = IntType,
                                        initializer = IntLiteral(10)
                                    ),
                                    BinaryOperation(
                                        left = VariableReference("a"),
                                        operator = BinaryOperator.PLUS,
                                        right = IntLiteral(5)
                                    )
                                )
                            ),
                            elseBranch = Block(
                                expressions = listOf(
                                    ConstantDeclaration(
                                        name = "b",
                                        type = IntType,
                                        initializer = IntLiteral(20)
                                    ),
                                    BinaryOperation(
                                        left = VariableReference("b"),
                                        operator = BinaryOperator.MINUS,
                                        right = IntLiteral(5)
                                    )
                                )
                            )
                        )
                    ),
                    VariableReference("y")
                )
            )
        )
    )
)
