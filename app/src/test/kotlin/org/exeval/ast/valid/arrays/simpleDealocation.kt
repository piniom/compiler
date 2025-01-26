package org.exeval.ast.valid.arrays

import org.exeval.ast.*
import org.exeval.ast.IntTypeNode

val SIMPLE_ARRAY_DEALOCATION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntTypeNode,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "x",
                        type = Array(
                            elementType = IntTypeNode
                        ),
                        initializer = MemoryNew(
                            type = Array(
                                elementType = IntTypeNode
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
                        type = IntTypeNode,
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