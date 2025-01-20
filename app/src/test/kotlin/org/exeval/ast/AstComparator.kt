package org.exeval.ast

class AstComparator {
	companion object {
		fun compareASTNodes(
			node1: ASTNode?,
			node2: ASTNode?,
		): Boolean {
			if (node1 == null || node2 == null) return node1 == node2

			if (node1::class != node2::class) return false

			// Compare the specific types of nodes
			val res =
				when (node1) {
					is Program -> compareProgram(node1, node2 as Program)
					is Block -> compareBlock(node1, node2 as Block)
					is VariableDeclarationBase -> compareVariableDeclarationBase(node1, node2 as VariableDeclarationBase)
					is Assignment -> compareAssignment(node1, node2 as Assignment)
					is Literal -> compareLiterals(node1, node2 as Literal)
					is VariableReference -> compareVariableReference(node1, node2 as VariableReference)
					is BinaryOperation -> compareBinaryOperation(node1, node2 as BinaryOperation)
					is UnaryOperation -> compareUnaryOperation(node1, node2 as UnaryOperation)
					is FunctionDeclaration -> compareFunctionDeclaration(node1, node2 as FunctionDeclaration)
					is FunctionCall -> compareFunctionCall(node1, node2 as FunctionCall)
					is Conditional -> compareConditional(node1, node2 as Conditional)
					is Loop -> compareLoop(node1, node2 as Loop)
					is Break -> compareBreak(node1, node2 as Break)
					is PositionalArgument -> comparePositionalArgument(node1, node2 as PositionalArgument)
					is NamedArgument -> compareNamedArgument(node1, node2 as NamedArgument)
					is MemoryNew -> compareMemoryNew(node1, node2 as MemoryNew)
					is MemoryDel -> compareMemoryDel(node1, node2 as MemoryDel)
					is ArrayAccess -> compareArrayAccess(node1, node2 as ArrayAccess)
					else -> throw IllegalStateException(
						"AstComparator does not know how to compare AstNode with this kind: ${node1::class}",
					)
				}
			return res
		}

		private fun compareArrayAccess(
			node1: ArrayAccess,
			node2: ArrayAccess,
		): Boolean {
			val isSameArray = compareASTNodes(node1.array, node2.array)
			val isSameIndex = compareASTNodes(node1.index, node2.index)
			return isSameArray && isSameIndex
		}

		private fun compareMemoryDel(
			node1: MemoryDel,
			node2: MemoryDel,
		): Boolean {
			val isSameExpr = compareASTNodes(node1.pointer, node2.pointer)
			return isSameExpr
		}

		private fun compareMemoryNew(
			node1: MemoryNew,
			node2: MemoryNew,
		): Boolean {
			val isSameType = node1.type == node2.type
			val areSameArguments =
				node1.constructorArguments.size == node2.constructorArguments.size &&
					node1.constructorArguments.zip(
						node2.constructorArguments,
					).all {
						compareASTNodes(it.first, it.second)
					}
			return isSameType && areSameArguments
		}

		private fun compareNamedArgument(
			node1: NamedArgument,
			node2: NamedArgument,
		): Boolean {
			val isSameName = node1.name == node2.name
			val isSameExpr = compareASTNodes(node1.expression, node2.expression)
			return isSameName && isSameExpr
		}

		private fun comparePositionalArgument(
			node1: PositionalArgument,
			node2: PositionalArgument,
		): Boolean {
			val isSameExpr = compareASTNodes(node1.expression, node2.expression)
			return isSameExpr
		}

		private fun compareProgram(
			node1: Program,
			node2: Program,
		): Boolean {
			return node1.functions.size == node2.functions.size &&
				node1.functions.zip(node2.functions)
					.all { compareASTNodes(it.first, it.second) }
		}

		private fun compareBlock(
			node1: Block,
			node2: Block,
		): Boolean {
			return node1.expressions.size == node2.expressions.size &&
				node1.expressions.zip(node2.expressions)
					.all { compareASTNodes(it.first, it.second) }
		}

		private fun compareVariableDeclarationBase(
			node1: VariableDeclarationBase,
			node2: VariableDeclarationBase,
		): Boolean {
			return node1.name == node2.name && node1.type == node2.type &&
				compareASTNodes(
					node1.initializer,
					node2.initializer,
				)
		}

		private fun compareAssignment(
			node1: Assignment,
			node2: Assignment,
		): Boolean {
			return compareASTNodes(node1.variable, node2.variable) && compareASTNodes(node1.value, node2.value)
		}

		private fun compareLiterals(
			node1: Literal,
			node2: Literal,
		): Boolean {
			return when {
				node1 is IntLiteral && node2 is IntLiteral -> node1.value == node2.value
				node1 is BoolLiteral && node2 is BoolLiteral -> node1.value == node2.value
				node1 is NopeLiteral && node2 is NopeLiteral -> true
				else -> false
			}
		}

		private fun compareVariableReference(
			node1: VariableReference,
			node2: VariableReference,
		): Boolean {
			return node1.name == node2.name
		}

		private fun compareBinaryOperation(
			node1: BinaryOperation,
			node2: BinaryOperation,
		): Boolean {
			return compareASTNodes(
				node1.left,
				node2.left,
			) && node1.operator == node2.operator && compareASTNodes(node1.right, node2.right)
		}

		private fun compareUnaryOperation(
			node1: UnaryOperation,
			node2: UnaryOperation,
		): Boolean {
			return node1.operator == node2.operator && compareASTNodes(node1.operand, node2.operand)
		}

		private fun compareFunctionDeclaration(
			node1: FunctionDeclaration,
			node2: FunctionDeclaration,
		): Boolean {
			return node1.name == node2.name && node1.parameters.size == node2.parameters.size &&
				node1.parameters.zip(
					node2.parameters,
				)
					.all { it.first.name == it.second.name && it.first.type == it.second.type } && node1.returnType == node2.returnType &&
				compareASTNodes(
					node1.body,
					node2.body,
				)
		}

		private fun compareFunctionCall(
			node1: FunctionCall,
			node2: FunctionCall,
		): Boolean {
			return node1.functionName == node2.functionName && node1.arguments.size == node2.arguments.size &&
				node1.arguments.zip(
					node2.arguments,
				).all { compareASTNodes(it.first, it.second) }
		}

		private fun compareConditional(
			node1: Conditional,
			node2: Conditional,
		): Boolean {
			return compareASTNodes(node1.condition, node2.condition) &&
				compareASTNodes(
					node1.thenBranch,
					node2.thenBranch,
				) && compareASTNodes(node1.elseBranch, node2.elseBranch)
		}

		private fun compareLoop(
			node1: Loop,
			node2: Loop,
		): Boolean {
			return node1.identifier == node2.identifier && compareASTNodes(node1.body, node2.body)
		}

		private fun compareBreak(
			node1: Break,
			node2: Break,
		): Boolean {
			return node1.identifier == node2.identifier && compareASTNodes(node1.expression, node2.expression)
		}
	}
}
