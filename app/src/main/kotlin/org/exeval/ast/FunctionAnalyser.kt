package org.exeval.ast

class FunctionAnalyser() {

    private var callGraph: MutableMap<AnyCallableDeclaration, MutableSet<AnyCallableDeclaration>> = mutableMapOf()
    private var staticParents: MutableMap<AnyCallableDeclaration, AnyCallableDeclaration?> = mutableMapOf()
    private var variableMap: MutableMap<AnyVariable, AnyCallableDeclaration> = mutableMapOf()
    private var isUsedInNested: MutableMap<AnyVariable, Boolean> = mutableMapOf()
    private var functionCallParent: MutableMap<FunctionCall, AnyCallableDeclaration> = mutableMapOf()
    private var functionChilds: MutableMap<AnyCallableDeclaration, MutableSet<AnyCallableDeclaration>> = mutableMapOf()
    private var globalFunctions: MutableSet<AnyCallableDeclaration> = mutableSetOf()

    public fun analyseFunctions(astInfo: AstInfo) : FunctionAnalysisResult {

        // Clear data structures
        callGraph = mutableMapOf()
        staticParents = mutableMapOf()
        variableMap = mutableMapOf()
        isUsedInNested = mutableMapOf()
        functionCallParent = mutableMapOf()
        functionChilds = mutableMapOf()
        globalFunctions = mutableSetOf()

        // Perform analisys from root
        analyseSubtree(astInfo.root, null)
        buildCallGraph()

        return FunctionAnalysisResult(
            callGraph = callGraph,
            staticParents = staticParents,
            variableMap = variableMap,
            isUsedInNested = isUsedInNested
        )
    }

    private fun buildCallGraph() {
        functionCallParent.forEach { (key, value) ->
            val funcDeclar = getFuctionCallToDeclaration(key, value)
            

            // add that func 'funcDeclar' is called inside of function 'value'
            callGraph[value]!!.add(funcDeclar!!)
        }
    }

    private fun getFuctionCallToDeclaration(call : FunctionCall, parentDeclaration : AnyCallableDeclaration?) : AnyCallableDeclaration? {
        
        // check if it global function call if it no longer have parent function
        if (parentDeclaration == null) 
            return globalFunctions.find {func -> checkIfFuncCallAndDeclarMatch(func, call) }
        
        // check if it is a parent function call
        if (checkIfFuncCallAndDeclarMatch(parentDeclaration, call))
            return parentDeclaration

        // check if it is one of child of parent function call
        val declarationInChildOfParent = functionChilds[parentDeclaration]!!.find { func -> checkIfFuncCallAndDeclarMatch(func, call) }
        if (declarationInChildOfParent != null)
            return declarationInChildOfParent

        // try finding in parent of parent
        return getFuctionCallToDeclaration(call, staticParents[parentDeclaration])
    }

    private fun checkIfFuncCallAndDeclarMatch(declaration : AnyCallableDeclaration, call : FunctionCall) : Boolean {
        if (declaration is AnyFunctionDeclaration) {
            return call.functionName == declaration.name && call.arguments.size == declaration.parameters.size
        } else {
            return false
        }
    }

    private fun analyseSubtree(node : ASTNode?, context : AnyCallableDeclaration?) {
        
        if (node == null)
            return

        // Check all needed types of nodes for analisys
        when (node) {
            is Program -> analyseProgramDeclaration(node, context)
            is FunctionDeclaration -> analyseFunctionDeclaration(node, context)
            is ConstructorDeclaration -> analyseConstructorDeclaration(node, context)
            is AnyVariable -> analyseAnyVariable(node, context)
            is Block -> analyseBlock(node, context)
            is Assignment -> analyseAssignment(node, context)
            is BinaryOperation -> analyseBinaryOperation(node, context)
            is UnaryOperation -> analyseUnaryOperation(node, context)
            is FunctionCall -> analyseFunctionCall(node, context)
            is Argument -> analyseArgument(node, context)
            is Conditional -> analyseConditional(node, context)
            is Loop -> analyseLoop(node, context)
            is Break -> analyseBreak(node, context)
            else -> {
                
            }
        }
    }

    private fun analyseBreak(breakStatement : Break, context : AnyCallableDeclaration?) {

        // Analyses break expression
        analyseSubtree(breakStatement.expression, context)
    }

    private fun analyseLoop(loop : Loop, context : AnyCallableDeclaration?) {
        
        // Analyses body of the loop
        analyseSubtree(loop.body, context)
    }

    private fun analyseConditional(conditional : Conditional, context : AnyCallableDeclaration?) {
        
        // Analyses all expressions of Conditional
        analyseSubtree(conditional.condition, context)
        analyseSubtree(conditional.thenBranch, context)
        analyseSubtree(conditional.elseBranch, context)
    }

    private fun analyseArgument(argument : Argument, context : AnyCallableDeclaration?) {

        // Analyses expression of arguments
        if (argument is NamedArgument) {
            analyseSubtree(argument.expression, context)
        }
        else if(argument is PositionalArgument) {
            analyseSubtree(argument.expression, context)
        }
    }

    private fun analyseFunctionCall(functionCall : FunctionCall, context : AnyCallableDeclaration?) {
        // Analyse of arguments of function call
        functionCall.arguments.forEach { argument ->
            analyseSubtree(argument, context)
        }

        // Call graph will be build after completing declaration analisys using this information
        functionCallParent[functionCall] = context!!

    }

    private fun analyseUnaryOperation(unaryOperation : UnaryOperation, context : AnyCallableDeclaration?) {

        // Analyse expression of operand
        analyseSubtree(unaryOperation.operand, context)
    }

    private fun analyseBinaryOperation(binaryOperation : BinaryOperation, context : AnyCallableDeclaration?) {
        
        // Analise both child of binary operation
        analyseSubtree(binaryOperation.left, context)
        analyseSubtree(binaryOperation.right, context)
    }

    private fun analyseAssignment(assignment : Assignment, context : AnyCallableDeclaration?) {

        // Analyse expression of the assignment
        analyseSubtree(assignment.value, context)
    }

    private fun analyseBlock(block : Block, context : AnyCallableDeclaration?) {
        
        // Analyse all expressions in block
        block.expressions.forEach { expression ->
            analyseSubtree(expression, context)
        }
    }

    private fun analyseFunctionDeclaration(declaration : FunctionDeclaration, context : AnyCallableDeclaration?) {
        
        // Assign function parent
        staticParents[declaration] = context

        // Initialize function call graph entry if not already present
        if (!callGraph.containsKey(declaration)) {
            callGraph[declaration] = mutableSetOf()
        }

        // Perform function's parameters
        declaration.parameters.forEach { param ->
            analyseSubtree(param, declaration)
        }

        if (context == null) {
            if(!functionChilds.containsKey(declaration))
                functionChilds[declaration] = mutableSetOf()
            globalFunctions.add(declaration)
        }
        else {
            if(!functionChilds.containsKey(declaration))
                functionChilds[declaration] = mutableSetOf()
            if(!functionChilds.containsKey(context))
                functionChilds[context] = mutableSetOf()
            functionChilds[context]!!.add(declaration)
        }

        // Continue analisys and change context
        analyseSubtree(declaration.body, declaration)
    }

    private fun analyseConstructorDeclaration(declaration : ConstructorDeclaration, context : AnyCallableDeclaration?) {

        // Assign function parent
        staticParents[declaration] = context

        // Initialize function call graph entry if not already present
        if (!callGraph.containsKey(declaration)) {
            callGraph[declaration] = mutableSetOf()
        }

        // Perform function's parameters
        declaration.parameters.forEach { param ->
            analyseSubtree(param, declaration)
        }

        if (context == null) {
            if(!functionChilds.containsKey(declaration))
                functionChilds[declaration] = mutableSetOf()
            globalFunctions.add(declaration)
        }
        else {
            if(!functionChilds.containsKey(declaration))
                functionChilds[declaration] = mutableSetOf()
            if(!functionChilds.containsKey(context))
                functionChilds[context] = mutableSetOf()
            functionChilds[context]!!.add(declaration)
        }

        // Continue analisys and change context
        analyseSubtree(declaration.body, declaration)
    }

    private fun analyseAnyVariable(variable : AnyVariable, context : AnyCallableDeclaration?) {
        if (context == null) {
            // Variable is used/declared outside of function so skipping it analisys
            return
        }

        // Assign all variable info
        if (variableMap.containsKey(variable))
            isUsedInNested[variable] = true
        else {
            variableMap[variable] = context
            isUsedInNested[variable] = false
        }

        // Check if declaration expression
        if (variable is VariableDeclarationBase)
            analyseSubtree(variable.initializer, context)
    }

    private fun analyseProgramDeclaration(program : Program, context : AnyCallableDeclaration?) {
        
        // Analyse all function of Program
        program.functions.forEach { function ->
            analyseSubtree(function, context)
        }

        program.structures.forEach { struct ->
            analyseSubtree(struct.constructorMethod, context)
        }
    }
}
