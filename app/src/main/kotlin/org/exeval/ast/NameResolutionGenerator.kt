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
        processAsBlock {
            pushLoopStack()
            processNode(astInfo.root)
            popLoopStack()
        }

        return OperationResult(NameResolution(
            breakToLoop,
            argumentToParam,
            functionToDecl,
            variableToDecl,
            assignmentToDecl
        ), diagnostics)
    }

    private fun processNode(astNode: ASTNode) {
        when (astNode) {
            is Break -> processBreak(astNode)
            is Loop -> processLoop(astNode)

            is FunctionCall -> processFnCall(astNode)
            is FunctionDeclaration -> processFnDecl(astNode)

            is ConstantDeclaration -> processConstDecl(astNode)
            is MutableVariableDeclaration -> processMutDecl(astNode)
            is Parameter -> processParameter(astNode)

            is Assignment -> getAssignmentType(astNode)
            is VariableReference -> getVariableReferenceType(astNode)

            is Program ->  processProgram(astNode)
            is Block ->  processBlock(astNode)
            is BinaryOperation -> processBinaryOp(astNode)
            is UnaryOperation -> processNode(astNode.operand)
            is Conditional -> processConditional(astNode)

            is Literal -> {}

            else -> addUnknownNodeError(astNode)
        }
    }

    private fun processAsBlock(block: () -> Unit)
    {
        pushBlock()
        block()
        popBlock()
    }


    private fun processProgram(program: Program) {
        processAsBlock { program.functions.forEach { processNode(it) } }
    }

    private fun processBlock(block: Block) {
        processAsBlock { block.expressions.forEach { processNode(it) } }
    }

    private fun processBinaryOp(astNode: BinaryOperation) {
        processAsBlock { processNode(astNode.left) }
        processAsBlock { processNode(astNode.right) }
    }

    private fun processConditional(astNode: Conditional) {
        processAsBlock { processNode(astNode.condition) }
        processAsBlock { processNode(astNode.thenBranch) }
        astNode.elseBranch?.let{ processAsBlock { processNode(it) } }
    }

    private fun getAssignmentType(assignment: Assignment) {
        getVarDecl(assignment, assignment.variable)?. let {
            assignmentToDecl[assignment] = it
        }

        processAsBlock { processNode(assignment.value) }
    }

    private fun getVariableReferenceType(varRef: VariableReference) {
        getVarDecl(varRef, varRef.name)?. let {
            variableToDecl[varRef] = it
        }
    }

    private fun processConstDecl(constDecl: ConstantDeclaration) {
        addDecl(constDecl.name, constDecl)
        processAsBlock { processNode(constDecl.initializer) }
    }

    private fun processMutDecl(mutDecl: MutableVariableDeclaration) {
        addDecl(mutDecl.name, mutDecl)
        mutDecl.initializer?.let {
            processAsBlock { processNode(it) }
        }
    }

    private fun processParameter(varDecl: Parameter) {
        addDecl(varDecl.name, varDecl)
    }

    private fun processFnCall(functionCall: FunctionCall) {
        getFnDecl(functionCall)?. let {
            functionToDecl[functionCall] = it
            assignArguments(functionCall, it)
        }

        functionCall.arguments.forEach{
            processAsBlock { processNode(it) }
        }
    }

    private fun assignArguments(call: FunctionCall, decl: FunctionDeclaration) {
        var hasNamed = false
        var positionalIdx = 0
        val usedParameters = mutableSetOf<Int>()

        call.arguments.forEach{
            when(it) {
                is PositionalArgument -> {
                    if (hasNamed) {
                        addPositionalAfterNamedArgumentError(it)
                        return
                    }
                    if(positionalIdx > decl.parameters.size) {
                        addToManyArgumentsError(call)
                        return
                    }

                    argumentToParam[it] = decl.parameters[positionalIdx]
                    usedParameters.add(positionalIdx)
                    positionalIdx++
                }
                is NamedArgument -> {
                    hasNamed = true

                    val idx = decl.parameters.indexOfFirst { param -> param.name == it.name }
                    if (idx < 0) {
                        addNamedArgNotFoundError(it)
                        return
                    }
                    if (usedParameters.contains(idx)) {
                        addAlreadyUsedArgError(it)
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
            addExpectedFunctionDeclarationError(functionCall)

        return null
    }

    private fun getVarDecl(node: ASTNode, name: String): AnyVariable? {
        val found = findDecl(name)

        if (found is AnyVariable)
            return found

        if(found == null)
            addUnknownVarError(node)
        else
            addExpectedVariableError(node)

        return null
    }

    private fun findDecl(name: String): ASTNode? {
        for (declInSingleBlock in declarations.reversed())
            declInSingleBlock[name]?.let { return it }

        return null
    }

    private fun processFnDecl(functionDecl: FunctionDeclaration) {
        addDecl(functionDecl.name, functionDecl)

        processAsBlock {
            pushLoopStack()
            functionDecl.parameters.forEach{ processNode(it) }
            processNode(functionDecl.body)
            popLoopStack()
        }
    }

    private fun processBreak(breakNode: Break) {
        breakNode.identifier?.let { processNamedBreak(breakNode, it) }
            ?: processSimpleBreak(breakNode)

        breakNode.expression?.let{ processAsBlock { processNode(it) } }
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
        processAsBlock { processNode( loopNode.body ) }
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
        addDiagnostic("Break statement must be inside a loop.", breakNode)
    }
    private fun addUnknownLoopIdentifierError(breakNode: Break) {
        addDiagnostic("Break use not existing loop identifier.", breakNode)
    }
    private fun addUnknownFunctionCallError(functionCall: FunctionCall) {
        addDiagnostic("Call of a not existing function.", functionCall)
    }
    private fun addExpectedFunctionDeclarationError(functionCall: FunctionCall) {
        addDiagnostic("Trying to call not callable thing.", functionCall)
    }
    private fun addUnknownVarError(node: ASTNode) {
        addDiagnostic("Use of a not existing variable.", node)
    }
    private fun addExpectedVariableError(node: ASTNode) {
        addDiagnostic("Trying to use something that is not a variable in a variable use context.", node)
    }
    private fun addUnknownNodeError(node: ASTNode) {
        addDiagnostic("Found unknown ASTNode", node)
    }
    private fun addPositionalAfterNamedArgumentError(argument: ASTNode) {
        addDiagnostic("Cannot use positional argument after named argument.", argument)
    }
    private fun addToManyArgumentsError(call: FunctionCall) {
        addDiagnostic("Trying to pass to many arguments to a function.", call)
    }
    private fun addNamedArgNotFoundError(argument: NamedArgument) {
        addDiagnostic("Cannot find a provided name of argument in function declaration (${argument.name}).", argument)
    }
    private fun addAlreadyUsedArgError(argument: ASTNode) {
        addDiagnostic("Trying to pass to an already provided parameter.", argument)
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

