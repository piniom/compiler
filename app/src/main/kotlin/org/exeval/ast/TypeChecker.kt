package org.exeval.ast

import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.OperationResult

class TypeChecker(private val astInfo: AstInfo, private val nameResolutionResult: NameResolution) {
    private val typeMap: MutableMap<Expr, Type> = mutableMapOf()
    private val diagnostics: MutableList<Diagnostics> = mutableListOf()

    private var activeLoop: Type? = null
    private val activeLoopMap: MutableMap<String, Type?> = mutableMapOf()

    private var activeStruct: StructType? = null

    public fun parse(): OperationResult<TypeMap> {
        innerParse(astInfo.root)

        return OperationResult(typeMap, diagnostics)
    }

    private fun innerParse(astNode: ASTNode): Type? {
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
            is ForeignFunctionDeclaration -> getForeignFunctionDeclarationType(astNode)
            is FunctionCall -> getFunctionCallType(astNode)

            is MemoryNew -> getMemoryNewType(astNode)
            is MemoryDel -> getMemoryDelType(astNode)
            is ArrayAccess -> getArrayAccessType(astNode)

            is HereReference -> getHereReference(astNode)
            is ConstructorDeclaration -> getConstructorDeclarationType(astNode)
            is StructTypeDeclaration -> getStructTypeDeclarationType(astNode)
            is StructFieldAccess -> getStructFieldAccessType(astNode)

            else -> addDiagnostic("Failed to find expression!", astNode)
        }

        return typeMap[astNode]
    }

    private fun calculateFieldOffset(index: Int): Long {
        return 8L * index
    }

    private fun calculateStructSize(fields: Map<String, Field>): Long {
        return 8L * fields.size
    }

    private fun convertTypeNodeToType(typeNode: org.exeval.ast.TypeNode): Type {
        return when (typeNode) {
            is IntTypeNode -> IntType
            is BoolTypeNode -> BoolType
            is NopeTypeNode -> NopeType
            is Array -> ArrayType(convertTypeNodeToType(typeNode.elementType))
            is TypeUse -> getTypeUseType(typeNode)
        }
    }

    private fun getTypeUseType(typeUse: TypeUse): Type {
        val structDeclaration = nameResolutionResult.typeNameToDecl[typeUse]

        if (structDeclaration == null) {
            addDiagnostic("Unknown type", typeUse)
            return NopeType
        }

        innerParse(structDeclaration)

        val fields = mutableMapOf<String, Field>()
        structDeclaration.fields.withIndex().forEach { (index, field) ->
            fields[field.name] = Field(
                type = convertTypeNodeToType(field.type),
                offset = calculateFieldOffset(index)
            )
        }

        val size = calculateStructSize(fields)
        return StructType(fields = fields, size = size)
    }


    private fun getStructFieldAccessType(fieldAccess: StructFieldAccess) {
        val structType = innerParse(fieldAccess.structObject)

        if (structType !is StructType) {
            addDiagnostic("Cannot access field", fieldAccess.structObject)
            typeMap[fieldAccess] = NopeType
            return
        }

        val field = structType.fields[fieldAccess.field]
        if (field == null) {
            addDiagnostic("Struct type does not contain field", fieldAccess)
            typeMap[fieldAccess] = NopeType
            return
        }

        typeMap[fieldAccess] = field.type
    }

    private fun getHereReference(hereReference: HereReference) {
        if (activeStruct == null) {
            addDiagnostic("'here' keyword used outside of a struct context", hereReference)
            typeMap[hereReference] = NopeType
            return
        }

        val field = activeStruct?.fields?.get(hereReference.field)
        if (field == null) {
            addDiagnostic("Struct does not contain field" + hereReference.field, hereReference)
            typeMap[hereReference] = NopeType
            return
        }

        typeMap[hereReference] = field.type
    }


    private fun getConstructorDeclarationType(constructorDeclaration: ConstructorDeclaration) {
        innerParse(constructorDeclaration.body)

        typeMap[constructorDeclaration] = NopeType
    }

    private fun getStructTypeDeclarationType(structDeclaration: StructTypeDeclaration) {
        val fields = structDeclaration.fields.withIndex().associate { (index, field) ->
            val resolvedType = innerParse(field) ?: NopeType

            field.name to Field(
                type = resolvedType,
                offset = calculateFieldOffset(index)
            )
        }

        val structType = StructType(fields = fields, size = calculateStructSize(fields))

        // Subtree has to know about current struct for here/self reference
        activeStruct = structType

        innerParse(structDeclaration.constructorMethod)

        activeStruct = null

        typeMap[structDeclaration] = structType
    }



    private fun getMemoryNewType(memoryNew: MemoryNew) {
        if (memoryNew.type !is Array) {
            addDiagnostic("Only Arrays are allowed with the new keyword", memoryNew)
        }
        if (memoryNew.constructorArguments.isEmpty()) {
            addDiagnostic("Missing new argument", memoryNew)
        }
        if (memoryNew.constructorArguments.size != 1) {
            addDiagnostic("Only one argument to new is allowed", memoryNew)
        }
        val argumentType = innerParse(memoryNew.constructorArguments[0].expression)
        if (argumentType != IntType) {
            addDiagnostic("Argument to new must be Int", memoryNew)
        }

        typeMap[memoryNew] = convertTypeNodeToType(memoryNew.type)
    }

    private fun getMemoryDelType(memoryDel: MemoryDel) {
        val pointerType = innerParse(memoryDel.pointer)
        if (pointerType !is ArrayType) {
            addDiagnostic("Only Arrays are allowed with the delete keyword", memoryDel)
        }

        typeMap[memoryDel] = NopeType
    }

    private fun getArrayAccessType(arrayAccess: ArrayAccess) {
        val arrayType = innerParse(arrayAccess.array)
        val indexType = innerParse(arrayAccess.index)
        if (arrayType !is ArrayType) {
            addDiagnostic("Only Arrays are allowed on the left side of the array access operator", arrayAccess)
        }
        if (indexType !is IntType) {
            addDiagnostic("Only Int is allowed as the index of the array access operator", arrayAccess)
        }

        if (arrayType is ArrayType) {
            typeMap[arrayAccess] = arrayType.elementType
        }
    }


    // Private methods                                           
    private fun getConditionalType(conditional: Conditional) {
        val conditionType = innerParse(conditional.condition)
        val thenType = innerParse(conditional.thenBranch)
        val elseType = conditional.elseBranch?.let { innerParse(it) }

        if (conditionType != BoolType) {
            addDiagnostic("Condition expression must be Bool", conditional.condition)
        }

        if (conditional.elseBranch == null) {
            if (thenType != NopeType) {
                addDiagnostic("Condition expression without else must be a Nope type", conditional.thenBranch)
            }
        } else {
            if (thenType != elseType) {
                addDiagnostic("Then and else branches must have the same type", conditional)
            }
        }

        thenType?.let { typeMap[conditional] = thenType }
    }

    private fun getBinaryOperationType(binaryOperation: BinaryOperation) {
        val leftType = innerParse(binaryOperation.left)
        val rightType = innerParse(binaryOperation.right)

        val expectedType = when (binaryOperation.operator) {
            BinaryOperator.PLUS, BinaryOperator.MINUS, BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE -> IntType
            BinaryOperator.AND, BinaryOperator.OR, BinaryOperator.EQ, BinaryOperator.GT, BinaryOperator.GTE, BinaryOperator.LT, BinaryOperator.LTE, BinaryOperator.NEQ -> BoolType
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
        val currLoop = activeLoop
        activeLoop = null
        loop.identifier?.let { activeLoopMap[it] = null }

        innerParse(loop.body)

        if (loop.identifier == null) {
            val currentActiveLoop = activeLoop
            if (currentActiveLoop == null) {
                // Loop type is NopeType if there is no break
                typeMap[loop] = NopeType
            } else {
                typeMap[loop] = currentActiveLoop
            }
        } else {
            val currentActiveLoop = activeLoop
            val targetLoopType = activeLoopMap[loop.identifier]
            if (currentActiveLoop == null && targetLoopType == null) {
                // Loop type is NopeType if there is no break
                typeMap[loop] = NopeType
            } else if (currentActiveLoop != null && targetLoopType != null) {
                if (currentActiveLoop != targetLoopType) {
                    addDiagnostic(
                        "Break type does not match the loop's type. Given: $currentActiveLoop, Expected: $targetLoopType",
                        loop
                    )
                } else {
                    typeMap[loop] = targetLoopType
                }
            } else if (currentActiveLoop != null) {
                typeMap[loop] = currentActiveLoop
            } else if (targetLoopType != null) {
                typeMap[loop] = targetLoopType
            }
        }

        activeLoop = currLoop
        loop.identifier?.let { activeLoopMap.remove(it) }
    }

    private fun getBreakType(breakEl: Break) {
        val identifier = breakEl.identifier
        val expressionType = breakEl.expression?.let { innerParse(it) } ?: NopeType

        // Check if break without label
        if (identifier == null) {
            // If first break then it defines loop type
            if (activeLoop == null) {
                activeLoop = expressionType
            }
            // If second or later break, then it has to match loop type
            else if (activeLoop != expressionType) {
                addDiagnostic("Break type does not match loop type", breakEl)
            }
        }
        // Check if break has label
        else {
            // If the loop with the label is not registered in the map
            if (!activeLoopMap.containsKey(identifier)) {
                addDiagnostic("Break targets an unknown or invalid loop", breakEl)
            }
            // If first break then it defines loop type
            if (activeLoopMap[identifier] == null) {
                activeLoopMap[identifier] = expressionType
            }
            // If second or later break, then it has to match loop type
            else if (activeLoopMap[identifier] != expressionType) {
                addDiagnostic("Break type does not match loop type", breakEl)
            }
        }

        expressionType?.let { typeMap[breakEl] = expressionType }
    }

    private fun getConstantDeclarationType(constantDeclaration: ConstantDeclaration) {
        val initializerType = innerParse(constantDeclaration.initializer)

        if (initializerType != convertTypeNodeToType(constantDeclaration.type)) {
            addDiagnostic("Initializer type does not match declared type", constantDeclaration.initializer)
        }

        typeMap[constantDeclaration] = NopeType
    }

    private fun getMutableVariableDeclarationType(mutableVariableDeclaration: MutableVariableDeclaration) {
        val initializerType = mutableVariableDeclaration.initializer?.let { innerParse(it) }

        if (initializerType != null && initializerType != convertTypeNodeToType(mutableVariableDeclaration.type)) {
            addDiagnostic(
                "Initializer type does not match declared type",
                mutableVariableDeclaration.initializer ?: mutableVariableDeclaration
            )
        }

        typeMap[mutableVariableDeclaration] = NopeType
    }

    private fun getVariableReferenceType(variableReference: VariableReference) {
        val variable = nameResolutionResult.variableToDecl[variableReference]
        val variableType = when (variable) {
            is ConstantDeclaration -> variable.type
            is MutableVariableDeclaration -> variable.type
            is Parameter -> variable.type
            else -> {
                addDiagnostic("Unknown variable declaration type", variableReference)
                NopeTypeNode
            }
        }

        typeMap[variableReference] = convertTypeNodeToType(variableType)
    }

    private fun getAssignmentType(assignment: Assignment) {
        val variable = nameResolutionResult.assignmentToDecl[assignment]
        val variableType = when (variable) {
            is ConstantDeclaration -> {
                innerParse(variable)
                variable.type
            }

            is MutableVariableDeclaration -> {
                innerParse(variable)
                variable.type
            }

            is Parameter -> variable.type
            else -> {
                addDiagnostic("Unknown variable assignment type", assignment)
                NopeTypeNode
            }
        }

        val valueType = innerParse(assignment.value)

        if (valueType != convertTypeNodeToType(variableType)) {
            addDiagnostic("Assignment type does not match variable type", assignment.value)
        }

        valueType?.let { typeMap[assignment] = NopeType }
    }

    private fun getFunctionDeclarationType(functionDeclaration: FunctionDeclaration) {
        val bodyType = innerParse(functionDeclaration.body)

        if (bodyType == null) {
            addDiagnostic("Could not determine function body type", functionDeclaration.body)
        }
        else if (bodyType != convertTypeNodeToType(functionDeclaration.returnType)) {
            addDiagnostic("Function return type does not match declared return type", functionDeclaration.body)
        }

        bodyType?.let { typeMap[functionDeclaration] = convertTypeNodeToType(functionDeclaration.returnType) }
    }

    private fun getForeignFunctionDeclarationType(functionDeclaration: ForeignFunctionDeclaration) {
        typeMap[functionDeclaration] = convertTypeNodeToType(functionDeclaration.returnType)
    }

    private fun getFunctionCallType(functionCall: FunctionCall) {
        val functionDecl = nameResolutionResult.functionToDecl[functionCall]

        if (functionDecl == null) {
            addDiagnostic("Function not found", functionCall)
            typeMap[functionCall] = NopeType
            return
        }

        val returnType = convertTypeNodeToType(functionDecl.returnType)
        typeMap[functionCall] = returnType

        var index = 0
        functionCall.arguments.filterIsInstance<PositionalArgument>().forEach { argument ->
            val parameterType = functionDecl?.parameters?.getOrNull(index)?.type?.let { convertTypeNodeToType(it) }
            val argumentType = innerParse(argument.expression)

            if (parameterType != argumentType) {
                addDiagnostic("Argument type does not match parameter type", argument.expression)
            }

            index += 1
        }

        val paramMap = functionDecl?.parameters?.associateBy { it.name }
        functionCall.arguments.filterIsInstance<NamedArgument>().forEach { argument ->
            val parameterType = paramMap?.get(argument.name)?.type?.let { convertTypeNodeToType(it) }
            val argumentType = innerParse(argument.expression)

            if (parameterType != argumentType) {
                addDiagnostic("Argument type does not match parameter type", argument.expression)
            }
        }
    }

    // Additional private methods

    private fun addDiagnostic(message: String, astNode: ASTNode) {
        val locationRange = astInfo.locations[astNode]

        if (locationRange != null) {
            diagnostics.add(
                SimpleDiagnostics(
                    message = message,
                    startLocation = locationRange.start,
                    stopLocation = locationRange.end
                )
            )
        }
    }
}