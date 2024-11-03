package org.exeval.ast

import AnyVariable
import Argument
import Assignment
import Break
import FunctionCall
import FunctionDeclaration
import Loop
import Parameter
import VariableReference

data class NameResolution(
    val breakToLoop: Map<Break, Loop>,
    val argumentToParam: Map<Argument, Parameter>,
    val functionToDecl: Map<FunctionCall, FunctionDeclaration>,
    val variableToDecl: Map<VariableReference, AnyVariable>,
    val assignmentToDecl: Map<Assignment, AnyVariable>
)