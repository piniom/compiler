package org.exeval.ast.valid.arrays

import org.exeval.ast.*

val PASS_ARRAY_TO_FUNCTION_AST = Program(
    functions = listOf(
        FunctionDeclaration(
            name = "main",
            parameters = emptyList(),
            returnType = IntType,
            body = Block(
                expressions = listOf(
                    ConstantDeclaration(
                        name = "x",
                        type = ArrayType(
                            elementType = IntType
                        ),
                        initializer = MemoryNew(
                            type = IntType,
                            constructorArguments = listOf(
                                PositionalArgument(IntLiteral(1))
                            ),
                        ),
                    ),
                    /// UNABLE TO MAKE ASSIGMENT TO ARRAY VARIABLE ,
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
        FunctionDeclaration(
            name = "getFirst",
            parameters = listOf(
                Parameter(
                    name = "arr",
                    type = ArrayType(
                        elementType = IntType
                    )
                ),
            ),
            returnType = IntType,
            body = ArrayAccess(
                array = VariableReference("arr"),
                index = IntLiteral(0),
            )
        )
    )
)


/*
foo getFirst(arr: [Int]) -> Int = { arr[0] }

// Should return 17
foo main() -> Int = {
    let x: [Int] = new [Int] (1);
    x[0] = 17;
    getFirst(arr)
}

*/