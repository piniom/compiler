package org.exeval.ast.valid.arrays

import org.exeval.ast.*
import org.exeval.ast.Int

val SIMPLE_ARRAY_DEALOCATION_AST = Program(
    functions = listOf(
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
                    ConstantDeclaration(
                        name = "y",
                        type = Int,
                        initializer = ArrayAccess(
                            array = VariableReference("x"),
                            index = IntLiteral(0),
                        ),
                    ),
                    MemoryDel(
                        pointer = VariableReference("x")
                    ),
                    VariableReference("y"),
                )
            )
        )
    )
)