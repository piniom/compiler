package org.exeval.ast.valid.indentifiers

import org.exeval.ast.*

val IDENTIFIERS_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "constant",
                        type = IntType,
                        initializer = IntLiteral(value = 0)
                    ),
                    MutableVariableDeclaration(
                        name = "variable",
                        type = IntType,
                        initializer = null
                    ),
                    ConstantDeclaration(
                        name = "longConstantName",
                        type = BoolType,
                        initializer = BoolLiteral(value = true)
                    ),
                    MutableVariableDeclaration(
                        name = "longVariableName",
                        type = BoolType,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "snaked_name",
                        type = IntType,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "numbered123",
                        type = IntType,
                        initializer = null
                    ),
                    ConstantDeclaration(
                        name = "uPPERCASE_CONSTANT",
                        type = IntType,
                        initializer = IntLiteral(value = 42)
                    ),
                    IntLiteral(value = 0)
                )
            )
        )
    )
)