package org.exeval.ast.valid.separator

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val SEPARATOR_VALUE_OF_FUNCTIONS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = FunctionDeclaration(
                            name = "f",
                            parameters = listOf(),
                            returnType = BoolTypeNode,
                            body = BoolLiteral(value = true)
                        ),
                        thenBranch = IntLiteral(value = 1),
                        elseBranch = IntLiteral(value = 2)
                    )
                )
            )
        )
    )
)