package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.DataLabel;
import org.exeval.cfg.interfaces.CFGNode;
import org.exeval.instructions.InstructionCovererInterface;

/*
interface CFGNode{
    val branches: Pair<CFGNode,CFGNode?>?
    val trees: List<Tree> 
}
*/

/*
data class BasicBlock (
    val label : DataLabel,
    val instructions : List<Instruction>,
    val successors : List<BasicBlock>
)
*/

/*
class InstructionCoverer(private val instructionPatterns : Map<OperationType, List<InstructionPattern>>) : InstructionCovererInterface {
    
    
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
                is Memory -> {
                    // label
                    return matchResult.createInstruction(listOf(), tree)
                }
                is Register -> {
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
        val registerChildren = matchResult.children.filterIsInstance(Register::class.java)
        val resultTree = when (tree) {
            is Assignable ->
                tree
            else ->
                null
        }
        return result + matchResult.createInstruction(registerChildren, resultTree)
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
            is Assignment ->{
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

*/

/*
package org.exeval.cfg

sealed interface Tree

sealed interface OperandArgumentType : Tree

data class Constant(val value: Int) : OperandArgumentType

sealed interface Assignable : OperandArgumentType

data class DataLabel(val name: String) : OperandArgumentType

data class Memory(val address: Tree) : Assignable
sealed class Register : Assignable {
    abstract val id: Int
}

data class VirtualRegister(override val id: Int) : Register()
data class PhysicalRegister(override val id: Int) : Register()
data class Assignment(val destination: Assignable, val value: Tree) : Tree

sealed interface OperationType

data class BinaryOperation(val left: Tree, val right: Tree, val operation: BinaryOperationType) : Tree
enum class BinaryOperationType : OperationType{
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, AND, OR, XOR, GREATER, GREATER_EQUAL, EQUAL, LESS, LESS_EQUAL, ASSIGNMENT
}

data class UnaryOp(val child: Tree, val operation: UnaryOperationType) : Tree
enum class UnaryOperationType : OperationType{
    NOT, MINUS, INCREMENT, DECREMENT, CALL
}

data object Call : Tree
data object Return : Tree
enum class NullaryOperationType : OperationType{
    RETURN
}

*/


/*
class Linearizer(private val instructionCoverer : InstructionCovererInterface) {

    public fun createBasicBlocks(node : CFGNode?) : List<BasicBlock> {
        return makeBasicBlocks(node).asReversed()
    }

    // create reversed list of basic blocks (revesed due to time saving)
    private fun makeBasicBlocks(node : CFGNode?) : MutableList<BasicBlock> {

        if (node == null)
            return mutableListOf()

        var basicContent = mutableListOf<Instruction>()
        for (tree in node.trees)
            basicContent.addAll(instructionCoverer.cover(tree))

        if (node.branches == null || node.branches?.first == null)
            return mutableListOf(BasicBlock(generateLabel(), basicContent, mutableListOf()))
        
        if (node.branches?.second == null) {
            var continuationList = makeBasicBlocks(node.branches?.first)
            var successors = mutableListOf<BasicBlock>()
            if (continuationList.firstOrNull() != null)
                successors.add(continuationList.first())
            continuationList.add(BasicBlock(generateLabel(), basicContent, successors))
            return continuationList
        }

        var firstContinuation = makeBasicBlocks(node.branches?.first)
        var secondContinuation = makeBasicBlocks(node.branches?.second)

        var successors = mutableListOf<BasicBlock>()
        if (firstContinuation.firstOrNull() != null)
            successors.add(firstContinuation.first())
        if (secondContinuation.firstOrNull() != null)
            successors.add(secondContinuation.first())

        secondContinuation.addAll(firstContinuation)
        secondContinuation.add(BasicBlock(generateLabel(), basicContent, successors))
        return secondContinuation
    }

    private fun generateLabel() : DataLabel {
        return DataLabel("")
    }
}*/