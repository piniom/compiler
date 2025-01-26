package org.exeval.ast.valid.comments

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val COMMENTS_SINGLE_LINE_COMMENT_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    MutableVariableDeclaration(
                        name = "result",
                        type = IntTypeNode,
                        initializer = IntLiteral(0)
                    ),
                    VariableReference("result")
                )
            )
        )
    )
)