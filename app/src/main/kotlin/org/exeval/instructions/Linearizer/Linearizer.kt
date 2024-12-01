package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.DataLabel;
import org.exeval.cfg.interfaces.CFGNode;
import org.exeval.instructions.InstructionCovererInterface;

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
            if (continuationList.lastOrNull() != null)
                successors.add(continuationList.last())
            continuationList.add(BasicBlock(generateLabel(), basicContent, successors))
            return continuationList
        }

        var firstContinuation = makeBasicBlocks(node.branches?.first)
        var secondContinuation = makeBasicBlocks(node.branches?.second)

        var successors = mutableListOf<BasicBlock>()
        if (firstContinuation.lastOrNull() != null)
            successors.add(firstContinuation.last())
        if (secondContinuation.lastOrNull() != null)
            successors.add(secondContinuation.last())

        secondContinuation.addAll(firstContinuation)
        secondContinuation.add(BasicBlock(generateLabel(), basicContent, successors))
        return secondContinuation
    }

    private fun generateLabel() : DataLabel {
        return DataLabel("")
    }
}