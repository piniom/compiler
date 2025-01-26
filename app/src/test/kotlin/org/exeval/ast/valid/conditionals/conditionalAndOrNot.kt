package org.exeval.ast.valid.conditionals

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val CONDITIONALS_CONDITIONAL_AND_OR_NOT_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = BinaryOperation(
                            left = BoolLiteral(true),
                            operator = BinaryOperator.AND,
                            right = UnaryOperation(
                                operator = UnaryOperator.NOT,
                                operand = BoolLiteral(false)
                            )
                        ),
                        thenBranch = IntLiteral(1),
                        elseBranch = IntLiteral(0)
                    )
                )
            )
        )
    )
)
