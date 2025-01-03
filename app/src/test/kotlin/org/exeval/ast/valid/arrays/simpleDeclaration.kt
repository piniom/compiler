package org.exeval.ast.valid.arrays

import org.exeval.ast.*

val SIMPLE_ARRAY_DECLARATION_AST = Program(
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
                                PositionalArgument(IntLiteral(2)),
                            ),
                        ),
                    ),
                    /// UNABLE TO MAKE ASSIGMENT TO ARRAY VARIABLE
                    BinaryOperation(
                        left = ArrayAccess(
                            array = VariableReference("x"),
                            index = IntLiteral(0),
                        ),
                        operator = BinaryOperator.PLUS,
                        right = ArrayAccess(
                            array = VariableReference("x"),
                            index = IntLiteral(1),
                        )
                    ),
                )
            )
        )
    )
)


/*
// Should return 3
foo main() -> Int = {
    let x: [Int] = new [Int] (2);
    x[0] = 1;
    x[1] = 2;
    x[0] + x[1]
}

*/