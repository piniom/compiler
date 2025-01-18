package org.exeval.ast.valid.comments

import org.exeval.ast.*

val COMMENTS_SINGLE_LINE_COMMENT_AST =
	Program(
		functions =
		listOf(
			FunctionDeclaration(
				name = "main",
				parameters = emptyList(),
				returnType = IntType,
				body =
				Block(
					expressions =
					listOf(
						MutableVariableDeclaration(
							name = "result",
							type = IntType,
							initializer = IntLiteral(0),
						),
						VariableReference("result"),
					),
				),
			),
		),
	)
