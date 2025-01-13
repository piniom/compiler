package org.exeval.ast.valid.indentifiers

import org.exeval.ast.*
import org.exeval.ast.Int

val IDENTIFIERS_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "constant",
                        type = Int,
                        initializer = IntLiteral(value = 0)
                    ),
                    MutableVariableDeclaration(
                        name = "variable",
                        type = Int,
                        initializer = null
                    ),
                    ConstantDeclaration(
                        name = "longConstantName",
                        type = Bool,
                        initializer = BoolLiteral(value = true)
                    ),
                    MutableVariableDeclaration(
                        name = "longVariableName",
                        type = Bool,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "snaked_name",
                        type = Int,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "numbered123",
                        type = Int,
                        initializer = null
                    ),
                    ConstantDeclaration(
                        name = "uPPERCASE_CONSTANT",
                        type = Int,
                        initializer = IntLiteral(value = 42)
                    ),
                    IntLiteral(value = 0)
                )
            )
        )
    )
)