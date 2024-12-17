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
import kotlin.collections.getOrDefault

class SpillHandler(
    val ic:InstructionCovererInterface,
    val swapRegisters: Set<PhysicalRegister>):SpillsHandler{
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
        val getIns = ins.usedRegisters().intersect(spills).flatMap{accMap[it]!!.get}
        val setIns = ins.definedRegisters().intersect(spills).flatMap{accMap[it]!!.set}
        val final = getIns + ins + setIns
        if(final.size == 1){
            return final
        }

        val mapping = (ins.usedRegisters() + ins.definedRegisters()).zip(swapRegisters).toMap()

        return final.map{changedInstruction(it,mapping)}
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
    /**[original] instruction with Registers swapped according to [mapping]*/
    private class changedInstruction(
        val original: Instruction,
        val mapping: Map<Register, PhysicalRegister>
    ): Instruction{
        override fun toAsm(mapping: Map<Register, PhysicalRegister>): String {
            val newMapping = mapping.map{
                (k,v)->Pair(mapping.getOrDefault(k,k),v)
            }.toMap()
            return original.toAsm(newMapping)
        }
        override fun usedRegisters(): List<Register> {
            return original.usedRegisters().map{mapping.getOrDefault(it,it)}
        }
        override fun definedRegisters(): List<Register> {
            return original.definedRegisters().map{mapping.getOrDefault(it,it)}
        }
        override fun isCopy(): Boolean {
            return original.isCopy()
        }
    }
}