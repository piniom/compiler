package org.exeval.ast.valid.variables

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val VARIABLES_REASIGNING_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "kInt",
                        type = IntTypeNode,
                        initializer = IntLiteral(value = 42)
                    ),
                    ConstantDeclaration(
                        name = "kBool",
                        type = BoolTypeNode,
                        initializer = BoolLiteral(value = true)
                    ),
                    ConstantDeclaration(
                        name = "kNope",
                        type = NopeTypeNode,
                        initializer = NopeLiteral()
                    ),
                    MutableVariableDeclaration(
                        name = "x",
                        type = IntTypeNode,
                        initializer = IntLiteral(value = 3)
                    ),
                    MutableVariableDeclaration(
                        name = "falsehood",
                        type = BoolTypeNode,
                        initializer = BoolLiteral(value = false)
                    ),
                    MutableVariableDeclaration(
                        name = "nope",
                        type = NopeTypeNode,
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
                    ),
                    IntLiteral(0),
                )
            )
        )
    )
)