package org.exeval.ast.valid.comments

import org.exeval.ast.*

val COMMENTS_MIXING_COMMENTS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    IntLiteral(0)
                )
            )
        )
    )
)