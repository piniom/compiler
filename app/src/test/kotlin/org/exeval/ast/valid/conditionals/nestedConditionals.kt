package org.exeval.ast.valid.conditionals

import org.exeval.ast.*
import org.exeval.ast.Int

val CONDITIONALS_NESTED_CONDITIONALS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = BoolLiteral(true),
                        thenBranch = Block(
                            expressions = listOf(
                                Conditional(
                                    condition = BoolLiteral(false),
                                    thenBranch = Block(listOf(IntLiteral(1))),
                                    elseBranch = Block(listOf(IntLiteral(2)))
                                )
                            )
                        ),
                        elseBranch = Block(
                            listOf(IntLiteral(3)))
                    )
                )
            )
        )
    )
)
