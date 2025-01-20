package org.exeval.instructions

import org.exeval.cfg.*

class InstructionCoverer(
	private val instructionPatterns: Map<InstructionPatternMapKey, List<InstructionPattern>>,
) : InstructionCovererInterface {
	override fun cover(
		tree: Tree,
		labelTrue: Label?,
	): List<Instruction> {
		val subtreeCost = mutableMapOf<Tree, Pair<Int, InstructionPattern?>>()
		val registerMap = mutableMapOf<Tree, VirtualRegister?>()
		computeCost(tree, subtreeCost, if (labelTrue == null) InstructionKind.EXEC else InstructionKind.JUMP)
		return coverTree(tree, subtreeCost.toMap(), registerMap, labelTrue)
	}

	private fun coverTree(
		tree: Tree,
		subtreeCost: Map<Tree, Pair<Int, InstructionPattern?>>,
		registerMap: MutableMap<Tree, VirtualRegister?>,
		labelTrue: Label?,
	): List<Instruction> {
		if (tree is RegisterTree || tree is ConstantTree) {
			return listOf()
		}
		val matchResult =
			subtreeCost[tree]!!
				.second!!
				.matches(tree)!!
		val register =
			when (tree) {
				is AssignmentTree, Return -> {
					registerMap[tree] = null
					null
				}

				else -> {
					val register = VirtualRegister()
					registerMap[tree] = register
					register
				}
			}

		if (matchResult.children.isEmpty()) {
			return matchResult.createInstruction(register, listOf(), labelTrue)
		}
		val childrenResults = matchResult.children.map { coverTree(it, subtreeCost, registerMap, labelTrue) }
		val result = mutableListOf<Instruction>()
		for (childResult in childrenResults) result.addAll(childResult)
		val childRegisters = mutableListOf<VirtualRegister>()
		for (child in matchResult.children) if (registerMap[child] != null) childRegisters.add(registerMap[child]!!)
		return result + matchResult.createInstruction(register, childRegisters, labelTrue)
	}

	private fun computeCost(
		tree: Tree,
		subtreeCost: MutableMap<Tree, Pair<Int, InstructionPattern?>>,
		instructionKind: InstructionKind,
	) {
		when (tree) {
			is BinaryOperationTree -> {
				computeCost(tree.left, subtreeCost, InstructionKind.VALUE)
				computeCost(tree.right, subtreeCost, InstructionKind.VALUE)
			}

			is UnaryOperationTree -> {
				computeCost(tree.child, subtreeCost, InstructionKind.VALUE)
			}

			is AssignmentTree -> {
				computeCost(tree.destination, subtreeCost, InstructionKind.VALUE)
				computeCost(tree.value, subtreeCost, InstructionKind.VALUE)
			}

			is MemoryTree -> {
				computeCost(tree.address, subtreeCost, InstructionKind.VALUE)
			}

			is StackPushTree -> {
				computeCost(tree.source, subtreeCost, InstructionKind.VALUE)
			}

			is StackPopTree -> {
				computeCost(tree.destination, subtreeCost, InstructionKind.VALUE)
			}

			else -> {
				// leaf
			}
		}
		var minCost = Int.MAX_VALUE
		var bestInstr: InstructionPattern? = null
		val candidatePatterns =
			instructionPatterns[InstructionPatternMapKey(tree.treeKind(), instructionKind)]
		if (candidatePatterns != null) {
			for (instructionPattern in candidatePatterns) {
				val result = instructionPattern.matches(tree)
				if (result != null) {
					var newCost = instructionPattern.cost
					for (child in result.children) {
						val childCost = subtreeCost[child]
						if (childCost?.second == null) {
							newCost = Int.MAX_VALUE
							break
						}
						newCost += childCost.first
					}
					if (minCost > newCost) {
						minCost = newCost
						bestInstr = instructionPattern
					}
				}
			}
		}
		subtreeCost[tree] = Pair(minCost, bestInstr)
	}
}
