package org.exeval.ast.valid.variables

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val VARIABLES_BASIC_OPERATIONS_OVER_VARIABLES_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = listOf(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "kUniversalConstant",
                        type = IntTypeNode,
                        initializer = IntLiteral(value = 42)
                    ),
                    MutableVariableDeclaration(
                        name = "a",
                        type = IntTypeNode,
                        initializer = IntLiteral(value = 5)
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.PLUS,
                            right = VariableReference(name = "kUniversalConstant")
                        )
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.PLUS,
                            right = VariableReference(name = "a")
                        )
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.PLUS,
                            right = VariableReference(name = "kUniversalConstant")
                        )
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.MINUS,
                            right = IntLiteral(value = 103)
                        )
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.MINUS,
                            right = VariableReference(name = "kUniversalConstant")
                        )
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.MULTIPLY,
                            right = IntLiteral(value = 2)
                        )
                    ),
                    Assignment(
                        variable = VariableReference("a"),
                        value = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.MULTIPLY,
                            right = VariableReference(name = "a")
                        )
                    ),
                    MutableVariableDeclaration(
                        name = "flag",
                        type = BoolTypeNode,
                        initializer = BinaryOperation(
                            left = VariableReference(name = "a"),
                            operator = BinaryOperator.GT,
                            right = VariableReference(name = "kUniversalConstant")
                        )
                    ),
                    Assignment(
                        variable = VariableReference("flag"),
                        value = UnaryOperation(
                            operator = UnaryOperator.NOT,
                            operand = VariableReference(name = "flag")
                        )
                    )
                )
            )
        )
    )
)