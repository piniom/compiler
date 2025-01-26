package org.exeval.ast.valid.indentifiers

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val IDENTIFIERS_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "constant",
                        type = IntTypeNode,
                        initializer = IntLiteral(value = 0)
                    ),
                    MutableVariableDeclaration(
                        name = "variable",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    ConstantDeclaration(
                        name = "longConstantName",
                        type = BoolTypeNode,
                        initializer = BoolLiteral(value = true)
                    ),
                    MutableVariableDeclaration(
                        name = "longVariableName",
                        type = BoolTypeNode,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "snaked_name",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    MutableVariableDeclaration(
                        name = "numbered123",
                        type = IntTypeNode,
                        initializer = null
                    ),
                    ConstantDeclaration(
                        name = "uPPERCASE_CONSTANT",
                        type = IntTypeNode,
                        initializer = IntLiteral(value = 42)
                    ),
                    IntLiteral(value = 0)
                )
            )
        )
    )
)