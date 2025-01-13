package org.exeval.ast.valid.indentifiers

import org.exeval.ast.*
import org.exeval.ast.Int

val IDENTIFIERS_FUNCTIONS_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = Int,
            body = IntLiteral(value = 0)
        ),
        FunctionDeclaration(
            name = "someFunction",
            parameters = listOf(),
            returnType = Nope,
            body = NopeLiteral()
        ),
        FunctionDeclaration(
            name = "withArguments",
            parameters = listOf(
                Parameter(name = "arg1", type = Int),
                Parameter(name = "longerNamed", type = Bool)
            ),
            returnType = Bool,
            body = BoolLiteral(value = false)
        ),
        FunctionDeclaration(
            name = "snaked_function",
            parameters = listOf(
                Parameter(name = "arg_1", type = Int),
                Parameter(name = "longer_named", type = Bool)
            ),
            returnType = Bool,
            body = BoolLiteral(value = true)
        ),
        FunctionDeclaration(
            name = "numbered123Name",
            parameters = listOf(),
            returnType = Nope,
            body = NopeLiteral()
        )
    )
)