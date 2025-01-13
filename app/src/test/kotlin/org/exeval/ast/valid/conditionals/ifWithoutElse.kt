package org.exeval.ast.valid.conditionals

import org.exeval.ast.*
import org.exeval.ast.Int

val CONDITIONALS_IF_WITHOUT_ELSE_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "x",
                        type = Int,
                        initializer = IntLiteral(10)
                    ),
                    Conditional(
                        condition = BoolLiteral(true),
                        thenBranch = Block(
                            expressions = listOf(
                                Assignment(
                                    variable = VariableReference("x"),
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
