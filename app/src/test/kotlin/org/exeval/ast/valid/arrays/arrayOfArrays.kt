package org.exeval.ast.valid.arrays

import org.exeval.ast.*

val ARRAY_OF_ARRAY_AST = Program(
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
                            elementType = ArrayType(
                                elementType = IntType
                            )
                        ),
                        initializer = MemoryNew(
                            type = ArrayType(
                                elementType = IntType
                            ),
                            constructorArguments = listOf(
                                PositionalArgument(IntLiteral(2))
                            ),
                        ),
                    ),
                    /// UNABLE TO MAKE ASSIGMENT TO ARRAY VARIABLE ,
                    BinaryOperation(
                        left = BinaryOperation(
                            left = BinaryOperation(
                                left = ArrayAccess(
                                    array = ArrayAccess(
                                        array = VariableReference("x"),
                                        index = IntLiteral(0),
                                    ),
                                    index = IntLiteral(0),
                                ),
                                operator = BinaryOperator.PLUS,
                                right = ArrayAccess(
                                    array = ArrayAccess(
                                        array = VariableReference("x"),
                                        index = IntLiteral(0),
                                    ),
                                    index = IntLiteral(1),
                                ),
                            ),
                            operator = BinaryOperator.PLUS,
                            right = BinaryOperation(
                                left = ArrayAccess(
                                    array = ArrayAccess(
                                        array = VariableReference("x"),
                                        index = IntLiteral(1),
                                    ),
                                    index = IntLiteral(0),
                                ),
                                operator = BinaryOperator.PLUS,
                                right = ArrayAccess(
                                    array = ArrayAccess(
                                        array = VariableReference("x"),
                                        index = IntLiteral(1),
                                    ),
                                    index = IntLiteral(1),
                                ),
                            ),
                        ),
                        operator = BinaryOperator.PLUS,
                        right = ArrayAccess(
                            array = ArrayAccess(
                                array = VariableReference("x"),
                                index = IntLiteral(1),
                            ),
                            index = IntLiteral(2),
                        )
                    )
                )
            )
        ),
    )
)


/*
// Should return 10
foo main() -> Int = {
    let x: [[Int]] = new [[Int]] (2);
    x[0] = new [Int] (2);
    x[1] = new [Int] (3);
    x[0][0] = 0;
    x[0][1] = 1;
    x[1][0] = 2;
    x[1][1] = 3;
    x[1][2] = 4;
    x[0][0] + x[0][1] + x[1][0] + x[1][1] + x[1][2]
}

*/