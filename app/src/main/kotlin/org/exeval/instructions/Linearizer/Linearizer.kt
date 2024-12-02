package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.DataLabel;
import org.exeval.cfg.interfaces.CFGNode;
import org.exeval.instructions.InstructionCovererInterface;

class Linearizer(private val instructionCoverer : InstructionCovererInterface) {

    public fun createBasicBlocks(node : CFGNode?) : List<BasicBlock> {
        var result = mutableListOf<BasicBlock>()
        makeBasicBlocks(node, result)
        return result.asReversed()
    }

    // create reversed list of basic blocks (revesed due to time saving)
    private fun makeBasicBlocks(node : CFGNode?, result : MutableList<BasicBlock>) {

        if (node == null)
            return

        var basicContent = mutableListOf<Instruction>()
        for (tree in node.trees)
            basicContent.addAll(instructionCoverer.cover(tree))

        if (node.branches == null || node.branches?.first == null) {
            result.add(BasicBlock(generateLabel(), basicContent, mutableListOf()))
            return
        }
        
        if (node.branches?.second == null) {
            makeBasicBlocks(node.branches?.first, result)
            var successors = mutableListOf<BasicBlock>()
            if (result.lastOrNull() != null)
                successors.add(result.last())
            result.add(BasicBlock(generateLabel(), basicContent, successors))
            return
        }

        
        var successors = mutableListOf<BasicBlock>()

        makeBasicBlocks(node.branches?.second, result)
        if (result.lastOrNull() != null)
            successors.add(result.last())

        makeBasicBlocks(node.branches?.first, result)
        if (result.lastOrNull() != null)
            successors.add(result.last())

        result.add(BasicBlock(generateLabel(), basicContent, successors))
    }

    private fun generateLabel() : DataLabel {
        return DataLabel("")
    }
}