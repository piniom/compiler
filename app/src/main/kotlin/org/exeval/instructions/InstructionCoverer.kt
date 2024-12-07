package org.exeval.instructions

import org.exeval.cfg.*

class InstructionCoverer(private val instructionPatterns : Map<TreeKind, List<InstructionPattern>>) : InstructionCovererInterface {


    override fun cover(tree : Tree, labelTrue: Label?) : List<Instruction> {
        var subtreeCost = mutableMapOf<Tree, Pair<Int, InstructionPattern?>>()
        var registerMap = mutableMapOf<Tree, VirtualRegister?>()
        computeCost(tree, subtreeCost)
        return coverTree(tree, subtreeCost.toMap(), registerMap)
    }

    private fun coverTree(tree: Tree, subtreeCost: Map<Tree, Pair<Int, InstructionPattern?>>, registerMap: MutableMap<Tree, VirtualRegister?>): List<Instruction> {
        val matchResult = subtreeCost[tree]!!.second!!.matches(tree)!!
        val register =  when (tree) {
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
            return matchResult.createInstruction(register, listOf())
        }
        val childrenResults = matchResult.children.map { coverTree(it, subtreeCost, registerMap) }
        var result = mutableListOf<Instruction>()
        for (childResult in childrenResults) result.addAll(childResult)
        var childRegisters = mutableListOf<VirtualRegister>()
        for(child in matchResult.children) if(registerMap[child] != null) childRegisters.add(registerMap[child]!!)
        return result + matchResult.createInstruction(register, childRegisters)
    }

    private fun computeCost(tree: Tree, subtreeCost: MutableMap<Tree, Pair<Int, InstructionPattern?>>) {
        when (tree) {
            is BinaryOperationTree -> {
                computeCost(tree.left, subtreeCost)
                computeCost(tree.right, subtreeCost)
            }

            is UnaryOperationTree -> {
                computeCost(tree.child, subtreeCost)
            }

            is AssignmentTree -> {
                computeCost(tree.destination, subtreeCost)
                computeCost(tree.value, subtreeCost)
            }

            else -> {
                // leaf
            }
        }
        var minCost = Int.MAX_VALUE
        var bestInstr: InstructionPattern? = null
        for (instructionPatternsPerOperation in instructionPatterns.values) {
            for (instructionPattern in instructionPatternsPerOperation) {
                val result = instructionPattern.matches(tree)
                if (result != null) {
                    var newCost = instructionPattern.cost
                    for (child in result.children){
                        val childCost = subtreeCost[child]!!.first
                        if (childCost == Int.MAX_VALUE){
                            newCost = Int.MAX_VALUE
                            break
                        }
                        newCost += childCost
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
