package org.exeval.ast.valid.arrays

import org.exeval.ast.*

val SIMPLE_ARRAY_DEALOCATION_AST = Program(
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
                    ConstantDeclaration(
                        name = "y",
                        type = IntType,
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


/*

// Should return 17
foo main() -> Int = {
    let x: [Int] = new [Int] (1);
    x[0] = 17;
    let y: Int = x[0];
    del x;
    y
}

*/