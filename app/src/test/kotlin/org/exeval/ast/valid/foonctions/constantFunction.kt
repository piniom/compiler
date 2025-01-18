package org.exeval.ast.valid.foonctions

import org.exeval.ast.*

val FOONCTIONS_CONSTANT_FUNCTION_AST =
	Program(
		functions =
		listOf(
			FunctionDeclaration(
				name = "g",
				parameters = emptyList(),
				returnType = IntType,
				body =
				Block(
					expressions =
					listOf(
						IntLiteral(3),
					),
				),
			),
			FunctionDeclaration(
				name = "main",
				parameters = emptyList(),
				returnType = IntType,
				body =
				Block(
					expressions =
					listOf(
						FunctionCall(
							functionName = "g",
							arguments = emptyList(),
						),
					),
				),
			),
		),
	)
