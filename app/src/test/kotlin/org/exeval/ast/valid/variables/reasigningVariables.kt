package org.exeval.ast.valid.variables

import org.exeval.ast.*

val VARIABLES_REASIGNING_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "kInt",
                        type = IntType,
                        initializer = IntLiteral(value = 42)
                    ),
                    ConstantDeclaration(
                        name = "kBool",
                        type = BoolType,
                        initializer = BoolLiteral(value = true)
                    ),
                    ConstantDeclaration(
                        name = "kNope",
                        type = NopeType,
                        initializer = NopeLiteral()
                    ),
                    MutableVariableDeclaration(
                        name = "x",
                        type = IntType,
                        initializer = IntLiteral(value = 3)
                    ),
                    MutableVariableDeclaration(
                        name = "falsehood",
                        type = BoolType,
                        initializer = BoolLiteral(value = false)
                    ),
                    MutableVariableDeclaration(
                        name = "nope",
                        type = NopeType,
                        initializer = NopeLiteral()
                    ),
                    Assignment(
                        variable = "x",
                        value = VariableReference(name = "kInt")
                    ),
                    Assignment(
                        variable = "falsehood",
                        value = VariableReference(name = "kBool")
                    ),
                    Assignment(
                        variable = "nope",
                        value = VariableReference(name = "kNope")
                    )
                )
            )
        )
    )
)