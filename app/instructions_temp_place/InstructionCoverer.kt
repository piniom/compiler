package org.exeval.instructions

import org.exeval.cfg.Tree
import org.exeval.cfg.TreeOperationType
import org.exeval.cfg.BinaryOperationTree
import org.exeval.cfg.UnaryOperationTree
import org.exeval.cfg.AssignmentTree
import org.exeval.cfg.RegisterTree
import org.exeval.cfg.NumericalConstantTree
import org.exeval.cfg.Label
import org.exeval.cfg.MemoryTree
import org.exeval.cfg.Call
import org.exeval.cfg.Return
import org.exeval.cfg.AssignableTree

class InstructionCoverer(private val instructionPatterns : Map<TreeOperationType, List<InstructionPattern>>) {
    
    
    public fun cover(tree : Tree) : List<Instruction> {
        var subtreeCost = mutableMapOf<Tree, Pair<Int, InstructionPattern?>>()
        computeCost(tree, subtreeCost)
        return coverTree(tree, subtreeCost.toMap())
    }

    private fun coverTree(tree: Tree, subtreeCost: Map<Tree, Pair<Int, InstructionPattern?>>) : List<Instruction>{
        val matchResult = subtreeCost[tree]!!.second!!.matches(tree)!!
        if (matchResult.children.isEmpty()){
            when(tree){
                is Call, Return ->{
                    // no tree
                    return matchResult.createInstruction(listOf(), null)
                }
                is MemoryTree -> {
                    // label
                    return matchResult.createInstruction(listOf(), tree)
                }
                is RegisterTree -> {
                    // register
                    return matchResult.createInstruction(listOf(), tree)
                }
                else -> {
                    throw IllegalArgumentException("Cover tree got unexpected tree: " + tree.toString())
                }
            } 
        }
        val childrenResults = matchResult.children.map { coverTree(it, subtreeCost) }
        var result = mutableListOf<Instruction>() 
        for(childResult in childrenResults) result.addAll(childResult)
        val registerTreeChildren = matchResult.children.filterIsInstance(RegisterTree::class.java)
        val resultTree = when (tree) {
            is AssignableTree ->
                tree
            else ->
                null
        }
        return result + matchResult.createInstruction(registerTreeChildren, resultTree)
    }

    private fun computeCost(tree: Tree, subtreeCost: MutableMap<Tree, Pair<Int, InstructionPattern?>>){
        when(tree){
            is BinaryOperationTree ->{
                computeCost(tree.left, subtreeCost) 
                computeCost(tree.right, subtreeCost) 
            }
            is UnaryOperationTree ->{
                computeCost(tree.child, subtreeCost)
            }
            is AssignmentTree ->{
                computeCost(tree.destination, subtreeCost)
                computeCost(tree.value, subtreeCost)
            }
            else -> {
                // leaf
            }
        }
        var minCost = Int.MAX_VALUE
        var bestInstr: InstructionPattern? = null
        for(instructionPatternsPerOperation in instructionPatterns.values){
           for(instructionPattern in instructionPatternsPerOperation){
                val result = instructionPattern.matches(tree)
                if (result != null){
                    //check overfloats
                    val newCost = instructionPattern.cost + result.children.mapNotNull { subtreeCost[it]!!.first }.sum()
                    if( minCost > newCost){
                        minCost = newCost 
                        bestInstr = instructionPattern
                    }
                }
            }
        }
        subtreeCost[tree] = Pair(minCost, bestInstr)
    }
}
