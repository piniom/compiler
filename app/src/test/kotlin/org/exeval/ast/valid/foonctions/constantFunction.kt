package org.exeval.ast.valid.foonctions

import org.exeval.ast.*
import org.exeval.ast.Int

val FOONCTIONS_CONSTANT_FUNCTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "g",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    IntLiteral(3)
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = Int,
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