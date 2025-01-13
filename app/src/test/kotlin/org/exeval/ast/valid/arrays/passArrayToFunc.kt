package org.exeval.ast.valid.arrays

import org.exeval.ast.*
import org.exeval.ast.Int

val PASS_ARRAY_TO_FUNCTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "getFirst",
            parameters = listOf(
                Parameter(
                    name = "arr",
                    type = Array(
                        elementType = Int
                    )
                ),
            ),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    ArrayAccess(
                        array = VariableReference("arr"),
                        index = IntLiteral(0)
                    )
                )
            )
        ),
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = Int,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "x",
                        type = Array(
                            elementType = Int
                        ),
                        initializer = MemoryNew(
                            type = Array(
                                elementType = Int
                            ),
                            constructorArguments = listOf(
                                PositionalArgument(IntLiteral(1))
                            ),
                        ),
                    ),
                    Assignment(
                        ArrayAccess(
                            array = VariableReference("x"),
                            index = IntLiteral(0),
                        ), 
                        IntLiteral(17),
                    ),
                    FunctionCall(
                        functionName = "getFirst",
                        arguments = listOf(
                            PositionalArgument(
                                VariableReference("x")
                            )
                        )
                    ),
                )
            )
        ),
    )
)
