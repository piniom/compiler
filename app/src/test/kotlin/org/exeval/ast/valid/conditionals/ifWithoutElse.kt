package org.exeval.ast.valid.conditionals

import org.exeval.ast.*

val CONDITIONALS_IF_WITHOUT_ELSE_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "x",
                        type = IntType,
                        initializer = IntLiteral(10)
                    ),
                    Conditional(
                        condition = BoolLiteral(true),
                        thenBranch = Block(
                            expressions = listOf(
                                Assignment(
                                    variable = "x",
                                    value = IntLiteral(0)
                                ),
                                NopeLiteral()
                            )
                        ),
                        elseBranch = null
                    ),
                    VariableReference("x")
                )
            )
        )
    )
)
