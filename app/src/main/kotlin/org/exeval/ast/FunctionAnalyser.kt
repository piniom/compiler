package org.exeval.ast

import StdlibDeclarationsCreator

class FunctionAnalyser() {

    private var callGraph: MutableMap<AnyFunctionDeclaration, MutableSet<AnyFunctionDeclaration>> = mutableMapOf()
    private var staticParents: MutableMap<FunctionDeclaration, FunctionDeclaration?> = mutableMapOf()
    private var variableMap: MutableMap<AnyVariable, FunctionDeclaration> = mutableMapOf()
    private var isUsedInNested: MutableMap<AnyVariable, Boolean> = mutableMapOf()
    private var functionCallParent: MutableMap<FunctionCall, FunctionDeclaration> = mutableMapOf()
    private var functionChilds: MutableMap<FunctionDeclaration, MutableSet<FunctionDeclaration>> = mutableMapOf()
    private var globalFunctions: MutableSet<AnyFunctionDeclaration> = mutableSetOf()

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
        StdlibDeclarationsCreator.getDeclarations().forEach {
            if (!callGraph.containsKey(it)) {
                callGraph[it] = mutableSetOf()
            }
            // Perform function's parameters
            it.parameters.forEach { param ->
                analyseSubtree(param, null)
            }

            globalFunctions.add(it)
        }

        buildCallGraph()

        return FunctionAnalysisResult(
            callGraph = callGraph as CallGraph,
            staticParents = staticParents,
            variableMap = variableMap,
            isUsedInNested = isUsedInNested
        )
    }

    private fun buildCallGraph() {
        functionCallParent.forEach { (key, value) ->
            var funcDeclar: AnyFunctionDeclaration? = getFuctionCallToDeclaration(key, value)

            // this value should always be not null otherwise it means there are not func declaration for this call
            if (funcDeclar != null)
                // add that func 'funcDeclar' is called inside of function 'value'
                callGraph[value]!!.add(funcDeclar)
        }
    }

    private fun getFuctionCallToDeclaration(call : FunctionCall, parentDeclaration : FunctionDeclaration?) : AnyFunctionDeclaration? {
        
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

    private fun checkIfFuncCallAndDeclarMatch(declaration : AnyFunctionDeclaration, call : FunctionCall) : Boolean {
        return call.functionName == declaration.name && call.arguments.size == declaration.parameters.size
    }

    private fun analyseSubtree(node : ASTNode?, context : FunctionDeclaration?) {
        
        if (node == null)
            return

        // Check all needed types of nodes for analisys
        when (node) {
            is Program -> analyseProgramDeclaration(node, context)
            is FunctionDeclaration -> analyseFunctionDeclaration(node, context)
            is ForeignFunctionDeclaration -> analyseForeignFunctionDeclaration(node, context)
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
        }
    }

    private fun analyseBreak(breakStatement : Break, context : FunctionDeclaration?) {

        // Analyses break expression
        analyseSubtree(breakStatement.expression, context)
    }

    private fun analyseLoop(loop : Loop, context : FunctionDeclaration?) {
        
        // Analyses body of the loop
        analyseSubtree(loop.body, context)
    }

    private fun analyseConditional(conditional : Conditional, context : FunctionDeclaration?) {
        
        // Analyses all expressions of Conditional
        analyseSubtree(conditional.condition, context)
        analyseSubtree(conditional.thenBranch, context)
        analyseSubtree(conditional.elseBranch, context)
    }

    private fun analyseArgument(argument : Argument, context : FunctionDeclaration?) {

        // Analyses expression of arguments
        if (argument is NamedArgument) {
            analyseSubtree(argument.expression, context)
        }
        else if(argument is PositionalArgument) {
            analyseSubtree(argument.expression, context)
        }
    }

    private fun analyseFunctionCall(functionCall : FunctionCall, context : FunctionDeclaration?) {

        // Function used outside of any other function (this case cannot happen)
        if (context == null)
            return 

        // Analyse of arguments of function call
        functionCall.arguments.forEach { argument ->
            analyseSubtree(argument, context)
        }

        // Call graph will be build after completing declaration analisys using this information
        functionCallParent[functionCall] = context

    }

    private fun analyseUnaryOperation(unaryOperation : UnaryOperation, context : FunctionDeclaration?) {

        // Analyse expression of operand
        analyseSubtree(unaryOperation.operand, context)
    }

    private fun analyseBinaryOperation(binaryOperation : BinaryOperation, context : FunctionDeclaration?) {
        
        // Analise both child of binary operation
        analyseSubtree(binaryOperation.left, context)
        analyseSubtree(binaryOperation.right, context)
    }

    private fun analyseAssignment(assignment : Assignment, context : FunctionDeclaration?) {

        // Analyse expression of the assignment
        analyseSubtree(assignment.value, context)
    }

    private fun analyseBlock(block : Block, context : FunctionDeclaration?) {
        
        // Analyse all expressions in block
        block.expressions.forEach { expression ->
            analyseSubtree(expression, context)
        }
    }

    private fun analyseFunctionDeclaration(declaration : FunctionDeclaration, context : FunctionDeclaration?) {
        
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
    private fun analyseForeignFunctionDeclaration(declaration : ForeignFunctionDeclaration, context : FunctionDeclaration?) {
        // Assign function parent
        TODO()
       //  staticParents[declaration] = context

       //  // Initialize function call graph entry if not already present
       //  if (!callGraph.containsKey(declaration)) {
       //      callGraph[declaration] = mutableSetOf()
       //  }

       //  // Perform function's parameters
       //  declaration.parameters.forEach { param ->
       //      analyseSubtree(param, declaration )
       //  }

       //  if (context == null) {
       //      if(!functionChilds.containsKey(declaration))
       //          functionChilds[declaration] = mutableSetOf()
       //      globalFunctions.add(declaration)
       //  }
       //  else {
       //      if(!functionChilds.containsKey(declaration))
       //          functionChilds[declaration] = mutableSetOf()
       //      if(!functionChilds.containsKey(context))
       //          functionChilds[context] = mutableSetOf()
       //      functionChilds[context]!!.add(declaration)
       //  }
    }
    
    private fun analyseAnyVariable(variable : AnyVariable, context : FunctionDeclaration?) {
        
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

    private fun analyseProgramDeclaration(program : Program, context : FunctionDeclaration?) {
        
        // Analyse all function of Program
        program.functions.forEach { function ->
            analyseSubtree(function, context)
        }
    }
}
