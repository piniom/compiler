package org.exeval.ast

data class NameResolution(
    val breakToLoop: Map<Break, Loop>,
    val argumentToParam: Map<Argument, Parameter>,
    val functionToDecl: Map<FunctionCall, AnyFunctionDeclaration>,
    val variableToDecl: Map<VariableReference, AnyVariable>,
    val assignmentToDecl: Map<Assignment, AnyVariable>,
    val typeNameToDecl: Map<TypeUse, StructTypeDeclaration>,
)
