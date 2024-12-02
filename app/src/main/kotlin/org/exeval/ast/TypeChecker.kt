package org.exeval.ast

import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.OperationResult

class TypeChecker(private val astInfo: AstInfo, private val nameResolutionResult: NameResolution) {
    private val typeMap: MutableMap<Expr, Type> = mutableMapOf()
    private val diagnostics: MutableList<Diagnostics> = mutableListOf()

    public fun parse() : OperationResult<TypeMap> {
        innerParse(astInfo.root)

        return OperationResult(typeMap, diagnostics)
    }

    private fun innerParse(astNode: ASTNode) : Type? {
        when (astNode) {
            is IntLiteral -> typeMap[astNode] = IntType
            is NopeLiteral -> typeMap[astNode] = NopeType
            is BoolLiteral -> typeMap[astNode] = BoolType

            is Conditional -> getConditionalType(astNode)
            is BinaryOperation -> getBinaryOperationType(astNode)
            is UnaryOperation -> getUnaryOperationType(astNode)

            is Program -> getProgramType(astNode)
            is Block -> getBlockType(astNode)
            is Loop -> getLoopType(astNode)
            is Break -> getBreakType(astNode)

            is ConstantDeclaration -> getConstantDeclarationType(astNode)
            is MutableVariableDeclaration -> getMutableVariableDeclarationType(astNode)
            is VariableReference -> getVariableReferenceType(astNode)
            is Assignment -> getAssignmentType(astNode)

            is FunctionDeclaration -> getFunctionDeclarationType(astNode)
            is FunctionCall -> getFunctionCallType(astNode)
            else -> addDiagnostic("Failed to find expression!", astNode)
        }

        return typeMap[astNode]
    }

    // Private methods                                           
    private fun getConditionalType(conditional: Conditional) {
        val conditionType = innerParse(conditional.condition)
        val thenType = innerParse(conditional.thenBranch)
        val elseType = conditional.elseBranch?.let { innerParse(it) }

        if (conditionType != BoolType) {
            addDiagnostic("Condition expression must be Bool", conditional.condition)
        }

        if (thenType != elseType) {
            addDiagnostic("Then and else branches must have the same type", conditional)
        }

        thenType?.let { typeMap[conditional] = thenType }
    }

    private fun getBinaryOperationType(binaryOperation: BinaryOperation) {
        val leftType = innerParse(binaryOperation.left)
        val rightType = innerParse(binaryOperation.right)

        val expectedType = when (binaryOperation.operator) {
            BinaryOperator.PLUS, BinaryOperator.MINUS, BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE -> IntType
            BinaryOperator.AND, BinaryOperator.OR, BinaryOperator.EQ, BinaryOperator.GT, BinaryOperator.GTE, BinaryOperator.LT, BinaryOperator.LTE -> BoolType
        }

        if (leftType == NopeType || rightType == NopeType) {
            addDiagnostic("One of operands is NopeType!", binaryOperation)
        }

        if (leftType != rightType) {
            addDiagnostic("Operands of binary operation must have the same type", binaryOperation)
        }

        if (expectedType == IntType && leftType != IntType) {
            addDiagnostic("Operands has to be numerical", binaryOperation)
        }

        typeMap[binaryOperation] = expectedType
    }

    private fun getUnaryOperationType(unaryOperation: UnaryOperation) {
        val operandType = innerParse(unaryOperation.operand)

        val expectedType = when (unaryOperation.operator) {
            UnaryOperator.MINUS -> IntType
            UnaryOperator.NOT -> BoolType
        }

        if (operandType == NopeType) {
            addDiagnostic("Operand is NopeType!", unaryOperation)
        }

        if (operandType != expectedType) {
            addDiagnostic("Operator and operand must both be the same type", unaryOperation)
        }

        typeMap[unaryOperation] = expectedType
    }

    private fun getProgramType(program: Program) {
        program.functions.forEach {
            innerParse(it)
        }
    }

    private fun getBlockType(block: Block) {
        block.expressions.forEach {
            innerParse(it)
        }

        val lastExpressionType = typeMap[block.expressions.last()]
        lastExpressionType?.let { typeMap[block] = lastExpressionType }
    }

    private fun getLoopType(loop: Loop) {
        val bodyType = innerParse(loop.body)

        bodyType?.let { typeMap[loop] = bodyType }
    }

    private fun getBreakType(breakEl: Break) {
        val expressionType = breakEl.expression?.let { innerParse(it) }

        expressionType?.let { typeMap[breakEl] = expressionType }
    }

    private fun getConstantDeclarationType(constantDeclaration: ConstantDeclaration) {
        val initializerType = innerParse(constantDeclaration.initializer)

        if (initializerType != constantDeclaration.type) {
            addDiagnostic("Initializer type does not match declared type", constantDeclaration.initializer)
        }

        typeMap[constantDeclaration] = constantDeclaration.type
    }

    private fun getMutableVariableDeclarationType(mutableVariableDeclaration: MutableVariableDeclaration) {
        val initializerType = mutableVariableDeclaration.initializer?.let { innerParse(it) }

        if (initializerType != mutableVariableDeclaration.type) {
            addDiagnostic("Initializer type does not match declared type", mutableVariableDeclaration.initializer ?: mutableVariableDeclaration)
        }

        typeMap[mutableVariableDeclaration] = mutableVariableDeclaration.type
    }

    private fun getVariableReferenceType(variableReference: VariableReference) {
        val variable = nameResolutionResult.variableToDecl[variableReference]
        val variableType = when (variable) {
            is ConstantDeclaration -> variable.type
            is MutableVariableDeclaration -> variable.type
            is Parameter -> variable.type
            else -> {
                addDiagnostic("Unknown variable declaration type", variableReference)
                NopeType
            }
        }

        typeMap[variableReference] = variableType
    }

    private fun getAssignmentType(assignment: Assignment) {
        val variable = nameResolutionResult.assignmentToDecl[assignment]
        val variableType = when (variable) {
            is ConstantDeclaration -> innerParse(variable)
            is MutableVariableDeclaration -> innerParse(variable)
            is Parameter -> variable.type
            else -> {
                addDiagnostic("Unknown variable assignment type", assignment)
                NopeType
            }
        }

        val valueType = innerParse(assignment.value)

        if (valueType != variableType) {
            addDiagnostic("Assignment type does not match variable type", assignment.value)
        }

        valueType?.let { typeMap[assignment] = NopeType }
    }

    private fun getFunctionDeclarationType(functionDeclaration: FunctionDeclaration) {
        val bodyType = innerParse(functionDeclaration.body)

        if (bodyType != functionDeclaration.returnType) {
            addDiagnostic("Function return type does not match declared return type", functionDeclaration.body)
        }

        bodyType?.let { typeMap[functionDeclaration] = functionDeclaration.returnType }
    }

    private fun getFunctionCallType(functionCall: FunctionCall) {
        val functionDecl = nameResolutionResult.functionToDecl[functionCall]

        val functionType = functionDecl?.let { innerParse(it) }

        if (functionCall.arguments.all { it is PositionalArgument }) {
            functionCall.arguments.forEachIndexed { index, argument ->
                val positionalArg = argument as PositionalArgument
                val parameterType = functionDecl?.parameters?.getOrNull(index)?.type
                val argumentType = innerParse(positionalArg.expression)

                if (parameterType != argumentType) {
                    addDiagnostic("Argument type does not match parameter type", positionalArg.expression)
                }
            }
        }

        if (functionCall.arguments.all { it is NamedArgument }) {
            val paramMap = functionDecl?.parameters?.associateBy { it.name }
            functionCall.arguments.forEach { argument ->
                val namedArg = argument as NamedArgument
                val parameterType = paramMap?.get(namedArg.name)?.type
                val argumentType = innerParse(namedArg.expression)

                if (parameterType != argumentType) {
                    addDiagnostic("Argument type does not match parameter type", namedArg.expression)
                }
            }
        }

        functionType?.let { typeMap[functionCall] = functionType }
    }

    // Additional private methods

    private fun addDiagnostic(message: String, astNode: ASTNode) {
        val locationRange = astInfo.locations[astNode]

        if (locationRange != null) {
            diagnostics.add(
                SimpleDiagnostics(
                    message = message,
                    startLocation = locationRange.start,
                    stopLocation = locationRange.end)
            )
        }
    }
}