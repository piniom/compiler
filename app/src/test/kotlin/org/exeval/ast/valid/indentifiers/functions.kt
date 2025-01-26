package org.exeval.ast.valid.indentifiers

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val IDENTIFIERS_FUNCTIONS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntTypeNode,
            body = IntLiteral(value = 0)
        ),
        FunctionDeclaration(
            name = "someFunction",
            parameters = listOf(),
            returnType = NopeTypeNode,
            body = NopeLiteral()
        ),
        FunctionDeclaration(
            name = "withArguments",
            parameters = listOf(
                Parameter(name = "arg1", type = IntTypeNode),
                Parameter(name = "longerNamed", type = BoolTypeNode)
            ),
            returnType = BoolTypeNode,
            body = BoolLiteral(value = false)
        ),
        FunctionDeclaration(
            name = "snaked_function",
            parameters = listOf(
                Parameter(name = "arg_1", type = IntTypeNode),
                Parameter(name = "longer_named", type = BoolTypeNode)
            ),
            returnType = BoolTypeNode,
            body = BoolLiteral(value = true)
        ),
        FunctionDeclaration(
            name = "numbered123Name",
            parameters = listOf(),
            returnType = NopeTypeNode,
            body = NopeLiteral()
        )
    )
)