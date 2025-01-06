package org.exeval.instructions

import org.exeval.cfg.*

class InstructionCoverer(private val instructionPatterns: Map<InstructionPatternMapKey, List<InstructionPattern>>) : InstructionCovererInterface {


    override fun cover(tree : Tree, labelTrue: Label?) : List<Instruction> {
		// println("covering ${tree}")
        val subtreeCost = mutableMapOf<Tree, Pair<Int, InstructionPattern?>>()
        val registerMap = mutableMapOf<Tree, VirtualRegister?>()
        computeCost(tree, subtreeCost, if(labelTrue == null) InstructionKind.EXEC else InstructionKind.JUMP)
        val instructions = coverTree(tree, subtreeCost.toMap(), registerMap, labelTrue)
		println("covered ${tree}")
		println("covering instructions: ${instructions}")
		return instructions
    }

    private fun coverTree(tree: Tree, subtreeCost: Map<Tree, Pair<Int, InstructionPattern?>>, registerMap: MutableMap<Tree, VirtualRegister?>, labelTrue : Label?): List<Instruction> {
		// println("tree: ${tree}")
		// println("subtreeCost: ${subtreeCost[tree]}")
		// println("pattern: ${subtreeCost[tree]!!.second}")
		// println("match result: ${subtreeCost[tree]!!.second!!.matches(tree)}")
        val matchResult = subtreeCost[tree]!!.second!!.matches(tree)!!
        val register =  when (tree) {
             is Return -> {
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
        for(child in matchResult.children) if(registerMap[child] != null) childRegisters.add(registerMap[child]!!)
		// println("register: ${register}")
        return result + matchResult.createInstruction(register, childRegisters, labelTrue)
    }

    private fun computeCost(tree: Tree, subtreeCost: MutableMap<Tree, Pair<Int, InstructionPattern?>>, instructionKind : InstructionKind) {
		// println("computing for tree: ${tree}")
        when (tree) {
			is RegisterTree -> {
				return
			}
			is ConstantTree -> {
				return
			}
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
				return
            }

            else -> {
                // leaf
            }
        }
        var minCost = Int.MAX_VALUE
        var bestInstr: InstructionPattern? = null
		val candidatePatterns =
			instructionPatterns[InstructionPatternMapKey(tree.treeKind(), instructionKind)]
		if (candidatePatterns == null) {
			throw NoSuchElementException(
				"No patterns with key (${tree.treeKind()}, ${instructionKind})"
			)
		}
		// println("candidates: ${candidatePatterns}")
        for (instructionPattern in candidatePatterns) {
            val result = instructionPattern.matches(tree)
            if (result != null) {
				// println("matched")
                var newCost = instructionPattern.cost
				// println("children: ${result.children}")
                for (child in result.children){
					val childCost = subtreeCost[child]
					// println("child: ${child}")
					// println("child cost: ${childCost}")
					if (childCost == null) {
						newCost = Int.MAX_VALUE
						break
					}
                    if (childCost.first == Int.MAX_VALUE){
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
			// else { println("not matched") }
        }
        subtreeCost[tree] = Pair(minCost, bestInstr)
		// println("assigning for ${tree}")
		// println("assigned ${subtreeCost[tree]}")
    }
}
