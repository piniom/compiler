package org.exeval.ast

import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.OperationResult

class NameResolutionGenerator(private val astInfo: AstInfo) {
    data class LoopStackData(var closestLoop: Loop? = null, val loopMap: MutableMap<String, Loop> = mutableMapOf())

    private var loopStack = ArrayDeque<LoopStackData>()
    private val diagnostics: MutableList<Diagnostics> = mutableListOf()

    private val declarations = ArrayDeque<MutableMap<String, ASTNode>>()

    private val breakToLoop: MutableMap<Break, Loop> = mutableMapOf()
    private val argumentToParam: MutableMap<Argument, Parameter> = mutableMapOf()
    private val functionToDecl: MutableMap<FunctionCall, FunctionDeclaration> = mutableMapOf()
    private val variableToDecl: MutableMap<VariableReference, AnyVariable> = mutableMapOf()
    private val assignmentToDecl: MutableMap<Assignment, AnyVariable> = mutableMapOf()


    fun parse(): OperationResult<NameResolution> {
        processNode(astInfo.root)

        return OperationResult(NameResolution(
            breakToLoop,
            argumentToParam,
            functionToDecl,
            variableToDecl,
            assignmentToDecl
        ), diagnostics)
    }

    private fun processNode(astNode: ASTNode) {
        pushBlock()
        when (astNode) {
            is Break -> processBreak(astNode)
            is Loop -> processLoop(astNode)

            is FunctionCall -> processFnCall(astNode)
            is FunctionDeclaration -> processFnDecl(astNode)

            is AnyVariable -> processVarDecl(astNode)
            is Assignment -> getAssignmentType(astNode)
            is VariableReference -> getVariableReferenceType(astNode)

            is Program ->  astNode.functions.forEach { processNode(it) }
            is Block ->  astNode.expressions.forEach { processNode(it) }
            is BinaryOperation -> processBinaryOp(astNode)
            is UnaryOperation -> processNode(astNode.operand)
            is Conditional -> processConditional(astNode)

            else -> addUnknownNodeError(astNode)
        }
        popBlock()
    }

    private fun processBinaryOp(astNode: BinaryOperation) {
        processNode(astNode.left)
        processNode(astNode.right)
    }

    private fun processConditional(astNode: Conditional) {
        processNode(astNode.condition)
        processNode(astNode.thenBranch)
        astNode.elseBranch?.let{ processNode(it) }
    }

    private fun getAssignmentType(assignment: Assignment) {
        getVarDecl(assignment, assignment.variable)?. let {
            assignmentToDecl[assignment] = it
        }

        processNode(assignment.value)
    }

    private fun getVariableReferenceType(varRef: VariableReference) {
        getVarDecl(varRef, varRef.name)?. let {
            variableToDecl[varRef] = it
        }
    }

    private fun processVarDecl(varDecl: AnyVariable) {
        addDecl(when(varDecl) {
            is VariableDeclarationBase -> varDecl.name
            is Parameter -> varDecl.name
        }, varDecl)
    }

    private fun processFnCall(functionCall: FunctionCall) {
        getFnDecl(functionCall)?. let {
            functionToDecl[functionCall] = it
            assignArguments(functionCall, it)
        }
    }

    private fun assignArguments(call: FunctionCall, decl: FunctionDeclaration) {
        var hasNamed = false
        var positionalIdx = 0;
        val usedParameters = mutableSetOf<Int>()

        call.arguments.forEach{
            when(it) {
                is PositionalArgument -> {
                    if (hasNamed) {
                        addPositionalAfterNamedArgumentError(it, call)
                        return;
                    }
                    if(positionalIdx > decl.parameters.size) {
                        addToManyArgumentsError(call)
                        return;
                    }

                    argumentToParam[it] = decl.parameters[positionalIdx]
                    usedParameters.add(positionalIdx)
                    positionalIdx++
                }
                is NamedArgument -> {
                    hasNamed = true;

                    val idx = decl.parameters.indexOfFirst { param -> param.name == it.name };
                    if (idx < 0) {
                        addNamedArgNotFoundError(it, call)
                        return
                    }
                    if (usedParameters.contains(idx)) {
                        addAlreadyUsedArgError(it, call)
                        return
                    }

                    usedParameters.add(idx)
                    argumentToParam[it] = decl.parameters[idx]
                }
            }
        }
    }

    private fun getFnDecl(functionCall: FunctionCall): FunctionDeclaration? {
        val found = findDecl(functionCall.functionName)

        if (found is FunctionDeclaration)
            return found

        if(found == null)
            addUnknownFunctionCallError(functionCall)
        else
            addExpectedFunctionDeclarationError(functionCall, found)

        return null
    }

    private fun getVarDecl(node: ASTNode, name: String): AnyVariable? {
        val found = findDecl(name)

        if (found is AnyVariable)
            return found

        if(found == null)
            addUnknownVarError(node)
        else
            addExpectedVariableError(node, found)

        return null
    }

    private fun findDecl(name: String): ASTNode? {
        for (declInSingleBlock in declarations)
            declInSingleBlock[name]?.let { return it }

        return null
    }

    private fun processFnDecl(functionDecl: FunctionDeclaration) {
        addDecl(functionDecl.name, functionDecl)
        pushBlock()
        pushLoopStack()
        functionDecl.parameters.forEach{ processNode(it) }
        processNode(functionDecl.body)
        popLoopStack()
        popBlock()
    }

    private fun processBreak(breakNode: Break) {
        breakNode.identifier?.let { processNamedBreak(breakNode, it) }
            ?: processSimpleBreak(breakNode)
    }

    private fun processNamedBreak(breakNode: Break, name: String) {
        getLoopData().loopMap[name]?.let { breakToLoop[breakNode] = it; }
            ?: addUnknownLoopIdentifierError(breakNode)
    }

    private fun processSimpleBreak(breakNode: Break) {
        getClosestLoop()?.let { breakToLoop[breakNode] = it; }
            ?: addBreakOutsideLoopError(breakNode)
    }


    private fun processLoop(loopNode: Loop) {
        val prevLoop = getClosestLoop()
        setClosestLoop(loopNode)
        loopNode.identifier?.let { getLoopData().loopMap[it] = loopNode }
        processNode(loopNode)
        setClosestLoop(prevLoop)
        loopNode.identifier?.let { getLoopData().loopMap.remove(it) }
    }

    private fun addDecl(name: String, node: ASTNode) {
        declarations.last()[name] = node
    }

    private fun pushBlock() {
        declarations.addLast(mutableMapOf())
    }

    private fun popBlock() {
        declarations.removeLast()
    }

    private fun setClosestLoop(loop: Loop?) {
        getLoopData().closestLoop = loop
    }

    private fun getClosestLoop(): Loop? {
        return getLoopData().closestLoop
    }

    private fun getLoopData(): LoopStackData {
        return loopStack.last()
    }

    private fun pushLoopStack() {
        loopStack.addLast(LoopStackData())
    }

    private fun popLoopStack() {
        loopStack.removeLast()
    }

    private fun addBreakOutsideLoopError(breakNode: Break) {
        addDiagnostic("", breakNode)
    }
    private fun addUnknownLoopIdentifierError(breakNode: Break) {
        addDiagnostic("", breakNode)
    }
    private fun addUnknownFunctionCallError(functionCall: FunctionCall) {
        addDiagnostic("", functionCall)
    }
    private fun addExpectedFunctionDeclarationError(functionCall: FunctionCall, found: ASTNode) {
        addDiagnostic("", functionCall)
    }
    private fun addUnknownVarError(node: ASTNode) {
        addDiagnostic("", node)
    }
    private fun addExpectedVariableError(node: ASTNode, found: ASTNode) {
        addDiagnostic("", node)
    }
    private fun addUnknownNodeError(node: ASTNode) {
        addDiagnostic("", node)
    }
    private fun addPositionalAfterNamedArgumentError(argument: ASTNode, call: FunctionCall) {
        addDiagnostic("", argument)
    }
    private fun addToManyArgumentsError(call: FunctionCall) {
        addDiagnostic("", call)
    }
    private fun addNamedArgNotFoundError(argument: ASTNode, call: FunctionCall) {
        addDiagnostic("", argument)
    }
    private fun addAlreadyUsedArgError(argument: ASTNode, call: FunctionCall) {
        addDiagnostic("", argument)
    }

    private fun addDiagnostic(message: String, astNode: ASTNode) {
        astInfo.locations[astNode]?.let {
            diagnostics.add(
                SimpleDiagnostics(
                    message = message,
                    startLocation = it.start,
                    stopLocation = it.end
                )
            )
        }
    }

}

