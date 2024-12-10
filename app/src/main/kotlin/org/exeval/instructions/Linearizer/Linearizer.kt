package org.exeval.instructions.linearizer;

import org.exeval.instructions.Instruction;
import org.exeval.cfg.Label;
import org.exeval.cfg.Register;
import org.exeval.cfg.PhysicalRegister;
import org.exeval.cfg.interfaces.CFGNode;
import org.exeval.instructions.InstructionCovererInterface;

class Linearizer(private val instructionCoverer : InstructionCovererInterface) {

    private var labelNum : Int = 0
    private var nodeToBBMap : MutableMap<CFGNode, BasicBlock> = mutableMapOf<CFGNode, BasicBlock>()

    public fun createBasicBlocks(node : CFGNode) : List<BasicBlock> {
        labelNum = 0
        nodeToBBMap = mutableMapOf<CFGNode, BasicBlock>()
        var result = mutableListOf<BasicBlock>()
        makeBasicBlocks(node, result)
        return result.asReversed()
    }

    // create reversed list of basic blocks (revesed due to time saving)
    private fun makeBasicBlocks(node : CFGNode, result : MutableList<BasicBlock>) : BasicBlock {
        
        if (nodeToBBMap.containsKey(node))
            return nodeToBBMap[node]!!

        var currentBB = BasicBlock(generateLabel(), mutableListOf(), mutableListOf())
        nodeToBBMap[node] = currentBB
        if (node.branches == null || node.branches?.first == null) {
            assignBasicBlock(currentBB, generateContent(node, null), mutableListOf())
            result.add(currentBB)
            return currentBB
        }

        if (node.branches?.second == null) {
            var child = makeBasicBlocks(node.branches?.first!!, result)
            var successors = listOf(child)
            var instructions = generateContent(node, null)
            if (result.lastOrNull() != child)
                instructions.add(JmpInstruction(child.label))
            assignBasicBlock(currentBB, instructions, successors)
            result.add(currentBB)
            return currentBB
        }

        var successors = mutableListOf<BasicBlock>()
        var ifBranch = makeBasicBlocks(node.branches?.first!!, result)
        var elseBranch = makeBasicBlocks(node.branches?.second!!, result)
        successors.add(elseBranch)
        successors.add(ifBranch)
        var instructions = generateContent(node, currentBB.label)
        if (result.lastOrNull() != elseBranch)
            instructions.add(JmpInstruction(elseBranch.label))

        assignBasicBlock(currentBB, instructions, successors)
        result.add(currentBB)
        return currentBB
    }

    private fun assignBasicBlock(block : BasicBlock, instructions : List<Instruction>, successors : List<BasicBlock>) {
        block.successors = successors
        block.instructions = instructions
    }

    private fun generateContent(node : CFGNode, label : Label?) : MutableList<Instruction> {
        var basicContent = mutableListOf<Instruction>()
        for (tree in node.trees)
            basicContent.addAll(instructionCoverer.cover(tree, label))
        return basicContent
    }

    private fun generateLabel() : Label {
        labelNum += 1
        return Label("LabelLin" + labelNum.toString())
    }

    private class JmpInstruction(private val label : Label) : Instruction {

        override fun toAsm(mapping: Map<Register, PhysicalRegister>): String {
            return "jmp " + label.name
        }

        override fun usedRegisters(): List<Register> {
            return listOf()
        }

        override fun definedRegisters(): List<Register> {
            return listOf()
        }

        override fun isCopy(): Boolean {
            return false
        }
    }
}
