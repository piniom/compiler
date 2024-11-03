package org.exeval.ast

import ASTNode
import Assignment
import BinaryOperation
import Block
import BoolLiteral
import Break
import Conditional
import ConstantDeclaration
import Expr
import FunctionCall
import FunctionDeclaration
import IntLiteral
import Loop
import MutableVariableDeclaration
import NopeLiteral
import Parameter
import Program
import UnaryOperation
import VariableReference
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.OperationResult

class TypeChecker(private val nameResolutionResult: NameResolution) {
    private val typeMap: MutableMap<Expr, Type> = mutableMapOf()
    private val diagnostics: List<Diagnostics> = mutableListOf()

    public fun parse(astNode: ASTNode) : OperationResult<TypeMap> {
        innerParse(astNode)

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
            else -> return null
        }

        return typeMap[astNode]
    }

    // Private methods                                           
    private fun getConditionalType(conditional: Conditional) {
        val conditionType = innerParse(conditional.condition)
        val thenType = innerParse(conditional.thenBranch)
        val elseType = conditional.elseBranch?.let { innerParse(it) }

        if (conditionType != BoolType) {
            //TODO: Error - expression has to be bool
        }

        if (thenType != elseType) {
            //TODO: Error - then and else has to be the same type
        }

        thenType?.let { typeMap[conditional] = thenType }
    }

    private fun getBinaryOperationType(binaryOperation: BinaryOperation) {
        var leftType = innerParse(binaryOperation.left)
        var rightType = innerParse(binaryOperation.right)

        //TODO ASK - what comes first
        if (leftType != rightType) {
            //TODO: Error - operations do not match
        }

        leftType?.let { typeMap[binaryOperation] = leftType }
    }

    private fun getUnaryOperationType(unaryOperation: UnaryOperation) {
        var operandType = innerParse(unaryOperation.operand)

        operandType?.let { typeMap[unaryOperation] = operandType }
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
            //TODO: Error - incorrect type
        }

        typeMap[constantDeclaration] = constantDeclaration.type
    }

    private fun getMutableVariableDeclarationType(mutableVariableDeclaration: MutableVariableDeclaration) {
        val initializerType = mutableVariableDeclaration.initializer?.let { innerParse(it) }

        if (initializerType != mutableVariableDeclaration.type) {
            //TODO: Error - incorrect type
        }

        typeMap[mutableVariableDeclaration] = mutableVariableDeclaration.type
    }

    private fun getVariableReferenceType(variableReference: VariableReference) {
        val variable = nameResolutionResult.variableToDecl[variableReference]
        val variableType = when (variable) {
            is ConstantDeclaration -> variable.type
            is MutableVariableDeclaration -> variable.type
            is Parameter -> variable.type
            else -> error("Unknown variable declaration type")
        }

        typeMap[variableReference] = variableType
    }

    private fun getAssignmentType(assignment: Assignment) {
        val variable = nameResolutionResult.assignmentToDecl[assignment]
        val variableType = when (variable) {
            is ConstantDeclaration -> innerParse(variable)
            is MutableVariableDeclaration -> innerParse(variable)
            is Parameter -> variable.type
            else -> error("Unknown variable declaration type")
        }

        val valueType = innerParse(assignment.value)

        if (valueType != variableType) {
            //TODO: Error - assigmenet type is incorrect
        }

        valueType?.let { typeMap[assignment] = valueType }
    }

    private fun getFunctionDeclarationType(functionDeclaration: FunctionDeclaration) {
        var bodyType = innerParse(functionDeclaration.body)

        if (bodyType != functionDeclaration.returnType) {
            //TODO: Error - body returns different type then declared return
        }

        bodyType?.let { typeMap[functionDeclaration] = functionDeclaration.returnType }
    }

    private fun getFunctionCallType(functionCall: FunctionCall) {
        var functionDecl = nameResolutionResult.functionToDecl[functionCall]

        var functionType = functionDecl?.let { innerParse(it) }

        functionType?.let { typeMap[functionCall] = functionType }
    }

}