package org.exeval.ast

import org.exeval.input.SimpleLocation
import org.exeval.utilities.LocationRange
import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.OperationResult
/*
WARNING: there is no AnyVariable to Associate a HereReference to in Assignment.
This means that AssignmentToDeclaration will not contain Assignments to HereReference, as well as its StructFieldAccess
If this is fine, then nothing needs to be done. If you want then associated, you need to:
1. add AnyVariable `Here` to StructTypeDeclaration
2. remove `if(variableName != consts.hereRef) {` in getAssignmentType
3. add `addDecl(consts.hereRef,{Struct's `Here` AnyVariable})` before `processNode(struct.constructorMethod)`
note that consts.hereRef contains an illegal symbol, therefore there should be no conflicts
*/

class NameResolutionGenerator(private val astInfo: AstInfo) {

    class consts {companion object{
        public val hereRef = ":here"
    }}

    data class LoopStackData(var closestLoop: Loop? = null, val loopMap: MutableMap<String, Loop> = mutableMapOf())

    private var loopStack = ArrayDeque<LoopStackData>()
    private val diagnostics: MutableList<Diagnostics> = mutableListOf()

    private val declarations = ArrayDeque<MutableMap<String, ASTNode>>()

    private val breakToLoop: MutableMap<Break, Loop> = mutableMapOf()
    private val argumentToParam: MutableMap<Argument, Parameter> = mutableMapOf()
    private val functionToDecl: MutableMap<FunctionCall, AnyFunctionDeclaration> = mutableMapOf()
    private val variableToDecl: MutableMap<VariableReference, AnyVariable> = mutableMapOf()
    private val assignmentToDecl: MutableMap<Assignment, AnyVariable> = mutableMapOf()
    private val useToStruct: MutableMap<TypeUse, StructTypeDeclaration> = mutableMapOf()


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
            assignmentToDecl,
            useToStruct,
        ), diagnostics)
    }

    private fun processNode(astNode: ASTNode) {
        when (astNode) {
            is Break -> processBreak(astNode)
            is Loop -> processLoop(astNode)

            is FunctionCall -> processFnCall(astNode)
            is FunctionDeclaration -> processFnDecl(astNode)
            is ForeignFunctionDeclaration -> processForeignFnDecl(astNode)
            is ConstructorDeclaration -> processConstructor(astNode)

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

            is NamedArgument -> processNamedArgument(astNode)
            is PositionalArgument -> processPositionalArgument(astNode)

            is StructTypeDeclaration -> processStructDecl(astNode)

            is Literal -> {}
            is HereReference -> {}

            is MemoryNew -> processMemoryNew(astNode)
            is MemoryDel -> processMemoryDel(astNode)
            is ArrayAccess -> processArrayAccess(astNode)
            is StructFieldAccess -> processStructFieldAccess(astNode)

            else -> addUnknownNodeError(astNode)
        }
    }


    private fun processStructDecl(struct: StructTypeDeclaration){
        if(hasSameVarAlreadyInScope(struct.name)){
            addStructRedefinitionError(struct)
            return
        }
        addDecl(struct.name,struct)
        if(struct.fields.groupBy{it.name}.any{it.value.size>1}){
            addDuplicateMemberError(struct)
            return
        }
        processAsBlock{
            struct.fields.forEach{
                processNode(it)
            }
            processNode(struct.constructorMethod)
        }
    }

    private fun processConstructor(constructor: ConstructorDeclaration){
        processAsBlock {
            pushLoopStack()
            processFunParameters(constructor.parameters)
            processNode(constructor.body)
            popLoopStack()
        }
    }

    private fun processAsBlock(block: () -> Unit) {
        pushBlock()
        block()
        popBlock()
    }

    private fun processNamedArgument(astNode: NamedArgument) {
        processAsBlock { processNode(astNode.expression) }
    }

    private fun processPositionalArgument(astNode: PositionalArgument) {
        processAsBlock { processNode(astNode.expression) }
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
        val variable = assignment.variable
        val variableName = when (variable) {
            is VariableReference -> variable.name
            is ArrayAccess -> getNameOfArrayAccess(variable)
            is StructFieldAccess -> getStructName(variable)
            else -> ""
        }
        if (variableName == "")
            addUnknownNodeError(variable)

        if(variableName != consts.hereRef) {
            getVarDecl(assignment, variableName)?.let {
                assignmentToDecl[assignment] = it
            }
        }

        processAsBlock { processNode(assignment.value) }
    }

    private fun getVariableReferenceType(varRef: VariableReference) {
        getVarDecl(varRef, varRef.name)?. let {
            variableToDecl[varRef] = it
        }
    }

    private fun processType(typeRef: Type, user: ASTNode){
        if(typeRef !is TypeUse){
            return
        }
        getStrDecl(typeRef.typeName,user)?.let{
            useToStruct[typeRef] = it
        }
    }

    private fun processConstDecl(constDecl: ConstantDeclaration) {
        processType(constDecl.type,constDecl)
        processVarDecl(constDecl, constDecl.name, constDecl.initializer)
    }

    private fun processMutDecl(mutDecl: MutableVariableDeclaration) {
        processType(mutDecl.type,mutDecl)
        processVarDecl(mutDecl, mutDecl.name, mutDecl.initializer)
    }

    private fun processVarDecl(varDecl: AnyVariable, name: String, inializer: ASTNode?) {
        if (hasSameVarAlreadyInScope(name)) {
            addSameVariableNameError(varDecl)
            return
        }

        addDecl(name, varDecl)
        inializer?.let {
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

    private fun assignArguments(call: FunctionCall, decl: AnyFunctionDeclaration) {
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

    private fun getNameOfArrayAccess(arr: ArrayAccess): String {
        val array = arr.array
        if (array is VariableReference)
            return array.name
        if (array is ArrayAccess)
            return getNameOfArrayAccess(array)
        addUnknownNodeError(array)
        return ""
    }

    private fun getStructName(str: StructFieldAccess): String {
        val struct = str.structObject
        if (struct is HereReference){
            return consts.hereRef
        }
        if (struct is VariableReference){
            return struct.name
        }
        if (struct is ArrayAccess){
            return getNameOfArrayAccess(struct)
        }
        addUnknownNodeError(str)
        return ""
    }

    private fun getFnDecl(functionCall: FunctionCall): AnyFunctionDeclaration? {
        val found = findDecl(functionCall.functionName)

        if (found is AnyFunctionDeclaration)
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

    private fun getStrDecl(name:String, using: ASTNode): StructTypeDeclaration? {
        val found = findDecl(name)

        if (found is StructTypeDeclaration){
            return found
        }

        addUnknownStructError(using)
        return null
    }

    private fun hasSameVarAlreadyInScope(name: String): Boolean {
        if (declarations.isEmpty())
            return false

        val scope = declarations.last()
        return scope.containsKey(name)
    }

    private fun hasSameLoopIdentifierInScope(name: String): Boolean {
        return loopStack.any { it.loopMap.containsKey(name) }
    }

    private fun findDecl(name: String): ASTNode? {
        for (declInSingleBlock in declarations.reversed())
            declInSingleBlock[name]?.let { return it }

        return null
    }

    private fun processFnDecl(functionDecl: FunctionDeclaration) {
        addDecl(functionDecl.name, functionDecl)
        processType(functionDecl.returnType,functionDecl)

        processAsBlock {
            pushLoopStack()
            processFunParameters(functionDecl.parameters)
            processNode(functionDecl.body)
            popLoopStack()
        }
    }

    private fun processForeignFnDecl(functionDecl: ForeignFunctionDeclaration) {
        addDecl(functionDecl.name, functionDecl)
        processType(functionDecl.returnType, functionDecl)

        processAsBlock {
            pushLoopStack()
            processFunParameters(functionDecl.parameters)
            popLoopStack()
        }
    }


    private fun processFunParameters(parameters: List<Parameter>) {
        parameters.forEach{
            processType(it.type, it)
            if (hasSameVarAlreadyInScope(it.name))
                addSameNameOfArgumentError(it)
            else
                addDecl(it.name, it)
        }
    }

    private fun processBreak(breakNode: Break) {
        breakNode.identifier?.let { processNamedBreak(breakNode, it) }
            ?: processSimpleBreak(breakNode)

        breakNode.expression?.let{ processAsBlock { processNode(it) } }
    }

    private fun processNamedBreak(breakNode: Break, name: String) {
        getLoopData().loopMap[name]?.let { breakToLoop[breakNode] = it; }
            ?: addUnknownLoopIdentifierError(breakNode, name)
    }

    private fun processSimpleBreak(breakNode: Break) {
        getClosestLoop()?.let { breakToLoop[breakNode] = it; }
            ?: addBreakOutsideLoopError(breakNode)
    }


    private fun processLoop(loopNode: Loop) {
        loopNode.identifier?.let {
            if (hasSameLoopIdentifierInScope(it)) {
                addSameLoopIdentifierError(loopNode)
                return
            }
        }

        val prevLoop = getClosestLoop()
        setClosestLoop(loopNode)
        loopNode.identifier?.let { getLoopData().loopMap[it] = loopNode }
        processAsBlock { processNode( loopNode.body ) }
        setClosestLoop(prevLoop)
        loopNode.identifier?.let { getLoopData().loopMap.remove(it) }
    }

    private fun processMemoryNew(memoryNew: MemoryNew) {
        processType(memoryNew.type,memoryNew)
        processAsBlock { memoryNew.constructorArguments.forEach { processNode(it) } }
    }

    private fun processMemoryDel(memoryDel: MemoryDel) {
        processAsBlock { processNode(memoryDel.pointer) }
    }

    private fun processArrayAccess(arrayAccess: ArrayAccess) {
        processAsBlock { processNode(arrayAccess.array) }
        processAsBlock { processNode(arrayAccess.index) }
    }

    private fun processStructFieldAccess(sfAccess: StructFieldAccess){
        processAsBlock{processNode(sfAccess.structObject)}
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
    private fun addUnknownLoopIdentifierError(breakNode: Break, identifier: String) {
        addDiagnostic("Break use not existing loop identifier (${identifier}).", breakNode)
    }
    private fun addUnknownFunctionCallError(functionCall: FunctionCall) {
        addDiagnostic("Call of a not existing function (${functionCall.functionName}).", functionCall)
    }
    private fun addExpectedFunctionDeclarationError(functionCall: FunctionCall) {
        addDiagnostic("Trying to call not callable thing (${functionCall.functionName}).", functionCall)
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
        addDiagnostic("Trying to pass too many arguments to a function.", call)
    }
    private fun addNamedArgNotFoundError(argument: NamedArgument) {
        addDiagnostic("Cannot find a provided name of argument in function declaration (${argument.name}).", argument)
    }
    private fun addAlreadyUsedArgError(argument: NamedArgument) {
        addDiagnostic("Trying to pass an already provided parameter (${argument.name}).", argument)
    }
    private fun addSameNameOfArgumentError(argument: ASTNode) {
        addDiagnostic("Cannot use the same name for argument twice.", argument)
    }
    private fun addSameVariableNameError(variable: ASTNode) {
        addDiagnostic("Cannot use the same name in the same scope for variable twice.", variable)
    }
    private fun addSameLoopIdentifierError(loop: Loop) {
        addDiagnostic("Cannot use the same identifier in nessted loop.", loop)
    }
    private fun addDuplicateMemberError(struct: StructTypeDeclaration){
        addDiagnostic("Member names have to be unique.",struct)
    }
    private fun addStructRedefinitionError(struct: StructTypeDeclaration){
        addDiagnostic("struct of this name already exists in scope.",struct)
    }
    private fun addUnknownStructError(using: ASTNode){
        addDiagnostic("Name of referenced type unknown.",using)
    }

    private fun addDiagnostic(message: String, astNode: ASTNode) {
        val noLocation = SimpleLocation(0, 0)
        val loc = astInfo.locations[astNode] ?: LocationRange(noLocation, noLocation)

        diagnostics.add(
            SimpleDiagnostics(
                message = message,
                startLocation = loc.start,
                stopLocation = loc.end
            )
        )
    }
}

