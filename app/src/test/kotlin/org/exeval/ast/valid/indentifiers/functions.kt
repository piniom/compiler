package org.exeval.ast.valid.indentifiers

import org.exeval.ast.*

val IDENTIFIERS_FUNCTIONS_AST =
	Program(
		functions =
		listOf(
			FunctionDeclaration(
				name = "main",
				parameters = listOf(),
				returnType = IntType,
				body = IntLiteral(value = 0),
			),
			FunctionDeclaration(
				name = "someFunction",
				parameters = listOf(),
				returnType = NopeType,
				body = NopeLiteral(),
			),
			FunctionDeclaration(
				name = "withArguments",
				parameters =
				listOf(
					Parameter(name = "arg1", type = IntType),
					Parameter(name = "longerNamed", type = BoolType),
				),
				returnType = BoolType,
				body = BoolLiteral(value = false),
			),
			FunctionDeclaration(
				name = "snaked_function",
				parameters =
				listOf(
					Parameter(name = "arg_1", type = IntType),
					Parameter(name = "longer_named", type = BoolType),
				),
				returnType = BoolType,
				body = BoolLiteral(value = true),
			),
			FunctionDeclaration(
				name = "numbered123Name",
				parameters = listOf(),
				returnType = NopeType,
				body = NopeLiteral(),
			),
		),
	)
