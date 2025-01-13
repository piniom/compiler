package org.exeval.ast.valid.variables

import org.exeval.ast.*
import org.exeval.ast.Int

val VARIABLES_REASIGNING_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "kInt",
                        type = Int,
                        initializer = IntLiteral(value = 42)
                    ),
                    ConstantDeclaration(
                        name = "kBool",
                        type = Bool,
                        initializer = BoolLiteral(value = true)
                    ),
                    ConstantDeclaration(
                        name = "kNope",
                        type = Nope,
                        initializer = NopeLiteral()
                    ),
                    MutableVariableDeclaration(
                        name = "x",
                        type = Int,
                        initializer = IntLiteral(value = 3)
                    ),
                    MutableVariableDeclaration(
                        name = "falsehood",
                        type = Bool,
                        initializer = BoolLiteral(value = false)
                    ),
                    MutableVariableDeclaration(
                        name = "nope",
                        type = Nope,
                        initializer = NopeLiteral()
                    ),
                    Assignment(
                        variable = VariableReference("x"),
                        value = VariableReference(name = "kInt")
                    ),
                    Assignment(
                        variable = VariableReference("falsehood"),
                        value = VariableReference(name = "kBool")
                    ),
                    Assignment(
                        variable = VariableReference("nope"),
                        value = VariableReference(name = "kNope")
                    )
                )
            )
        )
    )
)