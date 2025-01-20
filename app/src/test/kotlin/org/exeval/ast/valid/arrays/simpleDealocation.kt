package org.exeval.ast.valid.arrays

import org.exeval.ast.*

val SIMPLE_ARRAY_DEALOCATION_AST =
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
						ConstantDeclaration(
							name = "x",
							type =
							ArrayType(
								elementType = IntType,
							),
							initializer =
							MemoryNew(
								type =
								ArrayType(
									elementType = IntType,
								),
								constructorArguments =
								listOf(
									PositionalArgument(IntLiteral(1)),
								),
							),
						),
						Assignment(
							ArrayAccess(
								array = VariableReference("x"),
								index = IntLiteral(0),
							),
							IntLiteral(17),
						),
						ConstantDeclaration(
							name = "y",
							type = IntType,
							initializer =
							ArrayAccess(
								array = VariableReference("x"),
								index = IntLiteral(0),
							),
						),
						MemoryDel(
							pointer = VariableReference("x"),
						),
						VariableReference("y"),
					),
				),
			),
		),
	)
