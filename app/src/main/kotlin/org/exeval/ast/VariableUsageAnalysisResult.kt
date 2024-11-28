package org.exeval.ast

import org.exeval.ast.*

import org.exeval.ast.CallGraph
import org.exeval.ast.VariableUsage
import org.exeval.ast.NameResolution
import org.exeval.ast.FunctionAnalyser
import org.exeval.ast.NameResolutionGenerator

typealias VariableUsageAnalysisResult = Map<Expr, VariableUsage>

typealias rwSets = Pair<MutableSet<AnyVariable>,MutableSet<AnyVariable>>

class usageAnalysis{
    val callGraph:CallGraph
    var nameResolution: NameResolution
    var analysis = mutableMapOf<Expr, rwSets>()
    var enterFunc = false
    //NOTE: this wouldn't be necesarry it VariableUsageAnalysisResult, VariableUsage weren't read-only
    constructor(g:CallGraph,n:NameResolution){
        callGraph = g
        nameResolution = n
    }
    fun getAnalysisResult():VariableUsageAnalysisResult{
        return analysis.map{
            Pair(it.key,VariableUsage(it.value.first, it.value.second))
        }.toMap()
    }
    private fun translate(v:VariableReference):AnyVariable{
        require(nameResolution.variableToDecl.containsKey(v))
        return nameResolution.variableToDecl[v]!!
    }
    private fun translate(v:Assignment):AnyVariable{
        require(nameResolution.assignmentToDecl.containsKey(v))
        return nameResolution.assignmentToDecl[v]!!
    }
    private fun generateUsageAnalysis(node:Expr){
        if(!analysis.containsKey(node)){
            analysis[node] = Pair(mutableSetOf(),mutableSetOf())
        }
        when(node){
            is VariableReference -> {
                analysis[node] = Pair(mutableSetOf(translate(node)),mutableSetOf())
            }
            is Assignment -> {
                analysis[node] = Pair(mutableSetOf(),mutableSetOf(translate(node)))
                generateUsageAnalysis(node.value)
                analysis[node]!!.first += analysis[node.value]!!.first
                analysis[node]!!.second += analysis[node.value]!!.second
            }
            is Block -> {
                node.expressions.forEach{
                    generateUsageAnalysis(it)
                    analysis[node]!!.first  += analysis[it]!!.first
                    analysis[node]!!.second += analysis[it]!!.second
                }
            }
            is VariableDeclarationBase -> {
                node.initializer?.let{
                    generateUsageAnalysis(it)
                    analysis[node]!!.first  += analysis[it]!!.first
                    analysis[node]!!.second += analysis[it]!!.second
                }
            }
            is BinaryOperation -> {
                generateUsageAnalysis(node.left)
                generateUsageAnalysis(node.right)
                analysis[node]!!.first  += analysis[node.left]!!.first
                analysis[node]!!.second += analysis[node.left]!!.second
                analysis[node]!!.first  += analysis[node.right]!!.first
                analysis[node]!!.second += analysis[node.right]!!.second
            }
            is UnaryOperation -> {
                generateUsageAnalysis(node.operand)
                analysis[node]!!.first  += analysis[node.operand]!!.first
                analysis[node]!!.second += analysis[node.operand]!!.second
            }
            is Conditional -> {
                generateUsageAnalysis(node.condition)
                generateUsageAnalysis(node.thenBranch)
                node.elseBranch?.let{
                    generateUsageAnalysis(node.elseBranch)
                }
                analysis[node]!!.first  += analysis[node.condition]!!.first
                analysis[node]!!.second += analysis[node.condition]!!.second
                analysis[node]!!.first  += analysis[node.thenBranch]!!.first
                analysis[node]!!.second += analysis[node.thenBranch]!!.second
                node.elseBranch?.let{
                    analysis[node]!!.first  += analysis[node.elseBranch]!!.first
                    analysis[node]!!.second += analysis[node.elseBranch]!!.second
                }
            }
            is Loop -> {
                generateUsageAnalysis(node.body)
                analysis[node]!!.first  += analysis[node.body]!!.first
                analysis[node]!!.second += analysis[node.body]!!.second
            }
            is Break -> {
                node.expression?.let{
                    generateUsageAnalysis(node.expression)
                    analysis[node]!!.first  += analysis[node.expression]!!.first
                    analysis[node]!!.second += analysis[node.expression]!!.second
                }
            }
            is FunctionDeclaration -> {
                generateUsageAnalysis(node.body)
                analysis[node]!!.first += analysis[node.body]!!.first
                analysis[node]!!.second += analysis[node.body]!!.second
            }
            is FunctionCall -> {
                if(enterFunc){
                    val declaration = nameResolution.functionToDecl[node]!!
                    node.arguments.forEach{
                        //if argument's corresponding parameter is used, carry over this use
                        if(it is PositionalArgument){
                            generateUsageAnalysis(it.expression)
                            analysis[node]!!.first += analysis[it.expression]!!.first
                            analysis[node]!!.second += analysis[it.expression]!!.second
                            if(it.expression is VariableReference){
                                if(analysis[declaration]!!.first.contains(nameResolution.argumentToParam[it] as AnyVariable)){
                                    analysis[node]!!.first.add(nameResolution.variableToDecl[it.expression]!!)
                                }
                                if(analysis[declaration]!!.second.contains(nameResolution.argumentToParam[it] as AnyVariable)){
                                    analysis[node]!!.second.add(nameResolution.variableToDecl[it.expression]!!)
                                }
                            }
                        }
                        if(it is NamedArgument){
                            generateUsageAnalysis(it.expression)
                            analysis[node]!!.first += analysis[it.expression]!!.first
                            analysis[node]!!.second += analysis[it.expression]!!.second
                            if(it.expression is VariableReference){
                                if(analysis[declaration]!!.first.contains(nameResolution.argumentToParam[it] as AnyVariable)){
                                    analysis[node]!!.first.add(nameResolution.variableToDecl[it.expression]!!)
                                }
                                if(analysis[declaration]!!.second.contains(nameResolution.argumentToParam[it] as AnyVariable)){
                                    analysis[node]!!.second.add(nameResolution.variableToDecl[it.expression]!!)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
    //call this function to perform analysis
    fun run(root: Expr){
        //calculate without calls
        enterFunc = false
        callGraph.keys.forEach{
            generateUsageAnalysis(it)
        }
        //propagate function calls
        enterFunc = true
        var usageMap = callGraph.keys.associate{it to analysis[it]}
        while(true){
            callGraph.keys.forEach{
                generateUsageAnalysis(it)
            }
            var newMap = callGraph.keys.associate{it to analysis[it]}
            //if nothing has changed, then usage has propagated fully
            if(newMap == usageMap){
                break
            }else{
                usageMap = newMap
            }
        }
        generateUsageAnalysis(root)
    }
}
