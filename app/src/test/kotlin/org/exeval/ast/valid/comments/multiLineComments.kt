package org.exeval.ast.valid.comments

import org.exeval.ast.*
import org.exeval.ast.Int

val COMMENTS_MULTI_LINE_COMMENTS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "result",
                        type = Int,
                        initializer = IntLiteral(0)
                    ),
                    VariableReference("result")
                )
            )
        )
    )
)