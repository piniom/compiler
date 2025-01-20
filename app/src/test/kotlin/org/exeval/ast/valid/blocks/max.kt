package org.exeval.ast.valid.blocks

import org.exeval.ast.*

val BLOCK_MAX_AST =
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
							name = "a",
							type = IntType,
							initializer = null,
						),
						MutableVariableDeclaration(
							name = "b",
							type = IntType,
							initializer = null,
						),
						ConstantDeclaration(
							name = "max",
							type = IntType,
							initializer =
							Block(
								expressions =
								listOf(
									Conditional(
										condition =
										BinaryOperation(
											left = VariableReference("a"),
											operator = BinaryOperator.GT,
											right = VariableReference("b"),
										),
										thenBranch =
										Block(
											expressions =
											listOf(
												VariableReference("a"),
											),
										),
										elseBranch =
										Block(
											expressions =
											listOf(
												VariableReference("b"),
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
