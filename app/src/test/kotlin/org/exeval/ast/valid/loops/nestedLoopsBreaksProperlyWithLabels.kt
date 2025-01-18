package org.exeval.ast.valid.loops

import org.exeval.ast.*

val LOOPS_NESTED_LOOPS_BREAKS_PROPERLY_WITH_LABELS_AST =
	Program(
		functions =
		listOf(
			FunctionDeclaration(
				name = "main",
				parameters = listOf(),
				returnType = IntType,
				body =
				Block(
					expressions =
					listOf(
						Loop(
							identifier = "firstLoop",
							body =
							Block(
								expressions =
								listOf(
									Loop(
										identifier = "secondLoop",
										body =
										Block(
											expressions =
											listOf(
												Loop(
													identifier = "thirdLoop",
													body =
													Block(
														expressions =
														listOf(
															Break(
																identifier = "firstLoop",
																expression = IntLiteral(value = 3),
															),
														),
													),
												),
												Break(
													identifier = null,
													expression = IntLiteral(value = 2),
												),
											),
										),
									),
									Break(
										identifier = null,
										expression = IntLiteral(value = 1),
									),
								),
							),
						),
					),
				),
			),
		),
	)
