package org.exeval.ast.valid.conditionals

import org.exeval.ast.*

val CONDITIONALS_CONDITIONALS_INSIDE_LOOP_AST =
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
							name = "x",
							type = IntType,
							initializer = IntLiteral(0),
						),
						Loop(
							identifier = null,
							body =
							Block(
								expressions =
								listOf(
									Conditional(
										condition =
										BinaryOperation(
											left = VariableReference("x"),
											operator = BinaryOperator.GTE,
											right = IntLiteral(5),
										),
										thenBranch =
										Block(
											expressions =
											listOf(
												Break(
													identifier = null,
													expression = VariableReference("x"),
												),
											),
										),
										elseBranch =
										Block(
											expressions =
											listOf(
												Assignment(
													variable = VariableReference("x"),
													value =
													BinaryOperation(
														left = VariableReference("x"),
														operator = BinaryOperator.PLUS,
														right = IntLiteral(1),
													),
												),
											),
										),
									),
								),
							),
						),
					),
				),
			),
		),
	)
