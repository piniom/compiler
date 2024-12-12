package org.exeval.instructions

import org.exeval.cfg.AssignmentTree
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.cfg.RegisterTree
import org.exeval.cfg.VirtualRegister
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.instructions.interfaces.AllocationResult
import org.exeval.instructions.interfaces.LivenessChecker
import org.exeval.instructions.interfaces.RegisterAllocator
import org.exeval.instructions.interfaces.SpillsHandler
import org.exeval.instructions.linearizer.BasicBlock

class SpillHandler(val ic:InstructionCovererInterface):SpillsHandler{
    /**main function*/
    override fun handleSpilledVariables(blocks: List<BasicBlock>,
                                        ffm: FunctionFrameManager,
                                        spills: Set<VirtualRegister>): List<BasicBlock> {
        val nBlocks = blocks.toList()
        val accMap = spills.associate{it to getAccess(it,ffm)}
        nBlocks.forEach{ block ->
            block.instructions = block.instructions.flatMap{ inst ->
                handleInst(inst,spills,accMap)
            }
        }
        return nBlocks
    }
    private fun handleInst(ins: Instruction,
                           spills: Set<Register>,
                           accMap: Map<VirtualRegister, vrAccess>): List<Instruction>{
        val lSpills = getRegisters(ins).intersect(spills)
        val getIns = lSpills.flatMap{accMap[it]!!.get}
        val setIns = lSpills.flatMap{accMap[it]!!.set}
        return getIns + ins + setIns
    }
    /**class holding [get] and [set] instructions for Virtual Register [vr]*/
    private data class vrAccess(
        val get:List<Instruction>,
        val set:List<Instruction>,
        val vr: VirtualRegister)
    /**generate [vrAccess] for [vr]*/
    private fun getAccess(vr: VirtualRegister, ffm: FunctionFrameManager): vrAccess{
        val mem = ffm.alloc_frame_memory()
        val getIns = ic.cover(AssignmentTree(RegisterTree(vr),mem),null)
        val setIns = ic.cover(AssignmentTree(mem, RegisterTree(vr)),null)

        return vrAccess(getIns,setIns,vr)
    }
    /**return all [Register]'s used by [ins]*/
    private fun getRegisters(ins: Instruction): Set<Register>{
        return (ins.usedRegisters()+ins.definedRegisters()).toSet()
    }
}