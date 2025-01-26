package org.exeval.ast.valid.foonctions

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val FOONCTIONS_CONSTANT_FUNCTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "g",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    IntLiteral(3)
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    FunctionCall(
                        functionName = "g",
                        arguments = emptyList()
                    )
                )
            )
        )
    )
)