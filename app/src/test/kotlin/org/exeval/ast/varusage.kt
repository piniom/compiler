package org.exeval.ast

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class varusage {
	@Test
	fun upwardPropagationTest() {
		// upwards propagation
		val decl_b = MutableVariableDeclaration("b", IntType)
		val ref_b = VariableReference("b")
		val ast =
			Block(
				listOf(
					decl_b,
					ref_b,
				),
			)
		val nr =
			NameResolution(
				mapOf(), // break-loop
				mapOf(), // arg-param
				mapOf(), // function-decl
				mapOf(ref_b to decl_b), // variable-decl
				mapOf(), // assignment-decl
				mapOf(), // type-name-decl
			)
		val cg: CallGraph = mapOf()
		val a = usageAnalysis(cg, nr, ast)
		a.run()
		assert(a.getAnalysisResult()[ast]!!.read.contains(decl_b))
	}

	@Test
	fun argParamMappingTest() {
		// parameter translation
        /*
        f(a){
            a
        }
        b:int
        f(b)
         */
		val param = Parameter("a", IntType)
		val a_ref = VariableReference("a")
		val f_decl =
			FunctionDeclaration(
				"f",
				listOf(param),
				IntType,
				Block(
					listOf(
						a_ref,
					),
				),
			)
		val b_decl = MutableVariableDeclaration("b", IntType)
		val b_ref = VariableReference("b")
		val arg = PositionalArgument(b_ref)
		val call = FunctionCall("f", listOf(arg))
		val main =
			Block(
				listOf(
					f_decl,
					b_decl,
					call,
				),
			)
		val nr =
			NameResolution(
				mapOf(), // break-loop
				mapOf(arg to param), // arg-param
				mapOf(call to f_decl), // function-decl
				mapOf(
					b_ref to b_decl,
					a_ref to param,
				), // variable-decl
				mapOf(), // assignment-decl
				mapOf(), // type-name-decl
			)
		val cg: CallGraph = mapOf(f_decl to setOf())
		val a = usageAnalysis(cg, nr, main)
		a.run()
		assert(a.getAnalysisResult()[main]!!.read.contains(b_decl))
	}

	@Test
	fun functionPropagationTest() {
		// function propagation
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

		val a_param = Parameter("a", IntType)
		val a_ref = VariableReference("a")
		val a_arg = PositionalArgument(a_ref)
		val g_call = FunctionCall("g", listOf(a_arg))
		val f_decl =
			FunctionDeclaration(
				"f",
				listOf(a_param),
				IntType,
				Block(
					listOf(
						a_ref,
						g_call,
					),
				),
			)
		val b_param = Parameter("b", IntType)
		val b_ref = VariableReference("b")
		val b_arg = PositionalArgument(b_ref)
		val f_call = FunctionCall("f", listOf(b_arg))
		val g_decl =
			FunctionDeclaration(
				"f",
				listOf(b_param),
				IntType,
				Block(
					listOf(
						f_call,
					),
				),
			)
		val c_decl = MutableVariableDeclaration("c", IntType)
		val c_ref = VariableReference("c")
		val c_arg = PositionalArgument(c_ref)
		val f_call_main = FunctionCall("f", listOf(c_arg))
		val main =
			Block(
				listOf(
					f_decl,
					g_decl,
					c_decl,
					f_call_main,
				),
			)
		val nr =
			NameResolution(
				mapOf(), // break-loop
				mapOf(
					a_arg to b_param,
					b_arg to a_param,
					c_arg to a_param,
				), // arg-param
				mapOf(
					f_call to f_decl,
					f_call_main to f_decl,
					g_call to g_decl,
				), // function-decl
				mapOf(
					a_ref to a_param,
					b_ref to b_param,
					c_ref to c_decl,
				), // variable-decl
				mapOf(), // assignment-decl
				mapOf(), // type-name-decl
			)
		val cg: CallGraph =
			mapOf(
				f_decl to setOf(g_decl),
				g_decl to setOf(f_decl),
			)
		val a = usageAnalysis(cg, nr, main)
		a.run()
		assert(a.getAnalysisResult()[main]!!.read.contains(c_decl))
	}

	@Test
	fun complexTest() {
		val declaration = MutableVariableDeclaration("a", IntType)
		val aVarReference = VariableReference("a")
		val ast =
			Block(
				listOf(
					// 0
					declaration,
					// 1
					aVarReference,
					// 2
					Assignment(aVarReference, IntLiteral(2)),
					// 3
					Block(listOf(Assignment(aVarReference, aVarReference))),
					// 4
					MutableVariableDeclaration("b", IntType, Assignment(aVarReference, aVarReference)),
					// 5
					BinaryOperation(aVarReference, BinaryOperator.PLUS, Assignment(aVarReference, aVarReference)),
					// 6
					UnaryOperation(UnaryOperator.MINUS, Assignment(aVarReference, aVarReference)),
					// 7
					Conditional(BoolLiteral(true), Assignment(aVarReference, aVarReference)),
					// 8
					Loop(
						"l",
						Block(
							listOf(
								Assignment(aVarReference, aVarReference),
								Break("l", Assignment(aVarReference, aVarReference)),
							),
						),
					),
					// 9
					FunctionDeclaration(
						"f",
						listOf(Parameter("b", IntType)),
						IntType,
						Block(
							listOf(
								Assignment(aVarReference, VariableReference("b")),
								Assignment(aVarReference, aVarReference),
							),
						),
					),
					// 10
					FunctionCall("f", listOf(NamedArgument("b", Assignment(aVarReference, aVarReference)))),
				),
			)

		// REQUIRES FUNCTION ANALYSIS & NAME RESOLUTION
		val nr: NameResolution
		val cg: CallGraph
		run {
			val program = Program(listOf(FunctionDeclaration("main", listOf(), IntType, ast)))
			val astInfo = AstInfo(program, locations = emptyMap())
			val analyser = FunctionAnalyser()
			cg = analyser.analyseFunctions(astInfo).callGraph
			nr = NameResolutionGenerator(astInfo).parse().result
		}

		val a = usageAnalysis(cg, nr, ast)
		a.run()
		val l = a.getAnalysisResult()
		val readReq = (3..10).all { l[ast.expressions[it]]!!.read.contains(declaration) }
		val writeReq = (3..10).all { l[ast.expressions[it]]!!.write.contains(declaration) }
		val refReq = l[ast.expressions[1]]!!.read.contains(declaration)
		val assReq = l[ast.expressions[2]]!!.write.contains(declaration)
		val breakExpr = ((ast.expressions[8] as Loop).body as Block).expressions[1]
		val breakReq = l[breakExpr]!!.read.contains(declaration) && l[breakExpr]!!.write.contains(declaration)
		assert(readReq)
		assert(writeReq)
		assert(refReq)
		assert(assReq)
		assert(breakReq)
	}
}
