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
    companion object{
        //tests are here to ease debugging
        fun test1():Boolean{
            //upwards propagation
            val decl_b = MutableVariableDeclaration("b",IntType)
            val ref_b = VariableReference("b")
            val ast = Block(listOf(
                decl_b,
                ref_b)
            )
            val nr = NameResolution(
                mapOf(),//break-loop
                mapOf(),//arg-param
                mapOf(),//function-decl
                mapOf(ref_b to decl_b),//variable-decl
                mapOf()//assignment-decl
            )
            val cg: CallGraph = mapOf()
            val a = usageAnalysis(cg,nr)
            a.run(ast)
            return a.getAnalysisResult()[ast]!!.read.contains(decl_b)
        }
        fun test2():Boolean{
            //parameter translation
            /*
            f(a){
                a
            }
            b:int
            f(b)
            */
            val param = Parameter("a", IntType)
            val a_ref = VariableReference("a")
            val f_decl = FunctionDeclaration("f", listOf(param), IntType, Block(listOf(
                a_ref
            )))
            val b_decl = MutableVariableDeclaration("b", IntType)
            val b_ref = VariableReference("b")
            val arg = PositionalArgument(b_ref)
            val call = FunctionCall("f", listOf(arg))
            val main = Block(listOf(
                f_decl,
                b_decl,
                call
            ))
            val nr = NameResolution(
                mapOf(),//break-loop
                mapOf(arg to param),//arg-param
                mapOf(call to f_decl),//function-decl
                mapOf(  b_ref to b_decl,
                        a_ref to param),//variable-decl
                mapOf()//assignment-decl
            )
            val cg: CallGraph = mapOf(f_decl to setOf())
            val a = usageAnalysis(cg, nr)
            a.run(main)
            return a.getAnalysisResult()[main]!!.read.contains(b_decl)
        }
        fun test3():Boolean{
            //function propagation
            /*
            f(a){
                a
                g(a)
            }
            g(b){
                f(b)
            }
            c:int
            f(c)
            */

            val a_param = Parameter("a",IntType)
            val a_ref = VariableReference("a")
            val a_arg = PositionalArgument(a_ref)
            val g_call = FunctionCall("g", listOf(a_arg))
            val f_decl = FunctionDeclaration("f",listOf(a_param),IntType,Block(listOf(
                a_ref,
                g_call
            )))
            val b_param = Parameter("b",IntType)
            val b_ref = VariableReference("b")
            val b_arg = PositionalArgument(b_ref)
            val f_call = FunctionCall("f", listOf(b_arg))
            val g_decl = FunctionDeclaration("f",listOf(b_param),IntType,Block(listOf(
                f_call
            )))
            val c_decl = MutableVariableDeclaration("c", IntType)
            val c_ref = VariableReference("c")
            val c_arg = PositionalArgument(c_ref)
            val f_call_main = FunctionCall("f", listOf(c_arg))
            val main = Block(listOf(
                f_decl,
                g_decl,
                c_decl,
                f_call_main
            ))
            val nr = NameResolution(
                mapOf(),//break-loop
                mapOf(  a_arg to b_param,
                        b_arg to a_param,
                        c_arg to a_param),//arg-param
                mapOf(  f_call to f_decl,
                        f_call_main to f_decl,
                        g_call to g_decl),//function-decl
                mapOf(  a_ref to a_param,
                        b_ref to b_param,
                        c_ref to c_decl),//variable-decl
                mapOf()//assignment-decl
            )
            val cg: CallGraph = mapOf(
                f_decl to setOf(g_decl),
                g_decl to setOf(f_decl))
            val a = usageAnalysis(cg, nr)
            a.run(main)
            return a.getAnalysisResult()[main]!!.read.contains(c_decl)
        }
        //more pedantic tests
        fun test4():Boolean{
            val declaration = MutableVariableDeclaration("a", IntType)
            val ast = Block(listOf(
                /*0*/declaration,
                /*1*/VariableReference("a"),
                /*2*/Assignment("a", IntLiteral(2)),
                /*3*/Block(listOf(Assignment("a", VariableReference("a")))),
                /*4*/MutableVariableDeclaration("b", IntType,Assignment("a", VariableReference("a"))),
                /*5*/BinaryOperation(VariableReference("a"), BinaryOperator.PLUS, Assignment("a", VariableReference("a"))),
                /*6*/UnaryOperation(UnaryOperator.MINUS, Assignment("a", VariableReference("a"))),
                /*7*/Conditional(BoolLiteral(true), Assignment("a", VariableReference("a"))),
                /*8*/Loop("l",Block(listOf(
                    Assignment("a", VariableReference("a")),
                    Break("l",Assignment("a", VariableReference("a")))))),
                /*9*/FunctionDeclaration("f", listOf(Parameter("b", IntType)), IntType, Block(listOf(
                    Assignment("a", VariableReference("b")),
                    Assignment("a", VariableReference("a"))
                ))),
                /*10*/FunctionCall("f", listOf(NamedArgument("b", Assignment("a", VariableReference("a")))))
            ))

            //REQUIRES FUNCTION ANALYSIS & NAME RESOLUTION
            val nr:NameResolution
            val cg:CallGraph
            run{
                val program = Program(listOf(FunctionDeclaration("main", listOf(), IntType, ast)))
                val astInfo = AstInfo(program, locations = emptyMap())
                val analyser = FunctionAnalyser()
                cg = analyser.analyseFunctions(astInfo).callGraph
                nr = NameResolutionGenerator(astInfo).parse().result
            }

            
            val a = usageAnalysis(cg, nr)
            a.run(ast)
            val l = a.getAnalysisResult()
            val readReq  = (3..10).all{l[ast.expressions[it]]!!.read.contains(declaration)}
            val writeReq = (3..10).all{l[ast.expressions[it]]!!.write.contains(declaration)}
            val refReq = l[ast.expressions[1]]!!.read.contains(declaration)
            val assReq = l[ast.expressions[2]]!!.write.contains(declaration)
            val breakExpr = (ast.expressions[8] as Loop).body.expressions[1]
            val breakReq = l[breakExpr]!!.read.contains(declaration) && l[breakExpr]!!.write.contains(declaration)
            return readReq && writeReq && refReq && assReq && breakReq
        }
    }
}

fun main(){
    println(usageAnalysis.test4())
}
