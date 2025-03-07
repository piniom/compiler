package org.exeval.ast.valid.conditionals

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val CONDITIONALS_CONDITIONAL_CALLS_FUNCTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "greaterThan",
            parameters = listOf(
                Parameter(name = "a", type = IntTypeNode),
                Parameter(name = "b", type = IntTypeNode)
            ),
            returnType = BoolTypeNode,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = BinaryOperation(
                            left = VariableReference("a"),
                            operator = BinaryOperator.GT,
                            right = VariableReference("b")
                        ),
                        thenBranch = BoolLiteral(true),
                        elseBranch = BoolLiteral(false)
                    )
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    Conditional(
                        condition = FunctionCall(
                            functionName = "greaterThan",
                            arguments = listOf(
                                PositionalArgument(IntLiteral(10)),
                                PositionalArgument(IntLiteral(5))
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
