package org.exeval.instructions

import org.exeval.cfg.Tree
import org.exeval.cfg.BinaryOperation
import org.exeval.cfg.UnaryOp
import org.exeval.cfg.Assigment
import org.exeval.cfg.Register
import org.exeval.cfg.Constant
import org.exeval.cfg.Label
import org.exeval.cfg.Memory
import org.exeval.cfg.Call
import org.exeval.cfg.Return

class InstructionCoverer(private val instructionPatterns : Map<OperationType, List<InstructionPattern>>) {
    
    
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
                    return matchResult.createInstruction(null, listOf())
                } 
                is Memory -> {
                    // label
                    return matchResult.createInstruction(tree, listOf())
                }
                else -> {
                    // register    
                    return matchResult.createInstruction(tree, listOf())
                }
            } 
        }
        val childrenResults = matchResult.children.map { coverTree(it, subtreeCost) }
        var result = mutableListOf<Instruction>() 
        for(childResult in childrenResults) result.addAll(childResult)
        val registerChildren = matchResult.children.filterIsInstance(Register::class.java)
        val resultTree = if (tree is Call || tree is Return) null else tree
        return result + matchResult.createInstruction(resultTree, registerChildren)
    }

    private fun computeCost(tree: Tree, subtreeCost: MutableMap<Tree, Pair<Int, InstructionPattern?>>){
        when(tree){
            is BinaryOperation ->{
                computeCost(tree.left, subtreeCost) 
                computeCost(tree.right, subtreeCost) 
            }
            is UnaryOp ->{
                computeCost(tree.child, subtreeCost)
            }
            is Assigment ->{
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