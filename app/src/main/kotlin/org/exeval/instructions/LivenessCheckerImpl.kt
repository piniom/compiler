package org.exeval.instructions

import org.exeval.cfg.Register
import org.exeval.instructions.interfaces.LivenessChecker
import org.exeval.instructions.interfaces.LivenessResult
import org.exeval.instructions.linearizer.BasicBlock

class LivenessCheckerImpl: LivenessChecker {
    override fun check(basicBlocks: List<BasicBlock>): LivenessResult {
        var liveIn: MutableMap<Instruction, Set<Register>> = mutableMapOf()
        var liveOut: MutableMap<Instruction, Set<Register>> = mutableMapOf()

        val use: MutableMap<Instruction, Set<Register>> = mutableMapOf()
        val def: MutableMap<Instruction, Set<Register>> = mutableMapOf()
        var copyGraph: MutableMap<Register, MutableSet<Register>> = mutableMapOf()
        var interferenceGraph: MutableMap<Register, MutableSet<Register>> = mutableMapOf()
        for (bb in basicBlocks){
            for( instr in bb.instructions){
                use[instr] = instr.usedRegisters().toSet()
                def[instr] = instr.definedRegisters().toSet()
                liveOut[instr] = instr.definedRegisters().toSet()
                liveIn[instr] = instr.usedRegisters().toSet()

                for(r in (instr.usedRegisters() union instr.definedRegisters())){
                    if(!copyGraph.containsKey(r)) copyGraph[r] = mutableSetOf()
                    if(!interferenceGraph.containsKey(r)) interferenceGraph[r] = mutableSetOf()
                }

                if (instr.isCopy()){
                    // Not all instructions use and define at least one register
                    if (instr.definedRegisters().size > 0 && instr.usedRegisters().size > 0) {
                        copyGraph[instr.definedRegisters().first()]!!.add(instr.usedRegisters().first())
                        copyGraph[instr.usedRegisters().first()]!!.add(instr.definedRegisters().first())
                    }
                }
            }
        }
        var fixedPoint = false
        while(fixedPoint == false) {
            fixedPoint = true
            for (i in 0..<basicBlocks.size) {
                val bb = basicBlocks[i]
                for (j in 0..<bb.instructions.size) {
                    val next: List<Instruction> =
                        if (i == basicBlocks.size - 1 && j == bb.instructions.size - 1) listOf()
                        else if (j == bb.instructions.size - 1) bb.successors.map { b -> b.instructions.firstOrNull() }.filterNotNull()
                            .toList() as List<Instruction>
                        else listOf(bb.instructions[j + 1])
                    val instruction = bb.instructions[j]

                    val oldLiveIn = liveIn[instruction]!!
                    val oldLiveOut = liveOut[instruction]!!

                    val newLiveIn = use[instruction]!! union (oldLiveOut subtract def[instruction]!!)
                    var newLiveOut = def[instruction]!!
                    for (nextInstruction in next) {
                        val nextLiveIn = liveIn[nextInstruction]
                        if (nextLiveIn != null) {
                            newLiveOut = newLiveOut union nextLiveIn
                        }
                    }

                    liveIn[instruction] = newLiveIn
                    liveOut[instruction] = newLiveOut
                    if ( oldLiveIn != newLiveIn || oldLiveOut != newLiveOut)
                        fixedPoint = false
                }
            }
        }

        //edges
        for((instruction, registers) in def) {
            for (register in registers) {
                interferenceGraph[register]!!.addAll(liveOut[instruction]!!)
            }
        }
        //make undirected
        var igCopy = mutableMapOf<Register, Set<Register>>()
        igCopy.putAll(interferenceGraph)

        for((register1, list) in igCopy){
            for(register2 in list){
                interferenceGraph[register1]!!.add(register2)
                interferenceGraph[register2]!!.add(register1)
            }
        }

        //remove loops
        for(register in interferenceGraph.keys){
            interferenceGraph[register]!!.remove(register)
        }

        //remove CopyGraph edges from inferenceGraph
        for((register, list) in copyGraph){
            interferenceGraph[register]!!.removeAll(list)
        }
        return LivenessResult(interferenceGraph,  copyGraph)
    }

}