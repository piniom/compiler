package org.exeval.instructions

import org.exeval.cfg.AssignmentTree
import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.cfg.RegisterTree
import org.exeval.cfg.VirtualRegister
import org.exeval.ffm.interfaces.FunctionFrameManager
import org.exeval.instructions.interfaces.SpillsHandler
import org.exeval.instructions.linearizer.BasicBlock
import kotlin.collections.getOrDefault

class SpillHandler(
	val ic: InstructionCovererInterface,
	val swapRegisters: Set<PhysicalRegister>,
) : SpillsHandler {
	/**main function*/
	override fun handleSpilledVariables(
		blocks: List<BasicBlock>,
		ffm: FunctionFrameManager,
		spills: Set<VirtualRegister>,
	): List<BasicBlock> {
		val nBlocks = blocks.toList()
		val accMap = spills.associate { it to getAccess(it, ffm) }
		nBlocks.forEach { block ->
			block.instructions =
				block.instructions.flatMap { inst ->
					handleInst(inst, spills, accMap)
				}
		}
		return nBlocks
	}

	private fun handleInst(
		ins: Instruction,
		spills: Set<VirtualRegister>,
		accMap: Map<VirtualRegister, vrAccess>,
	): List<Instruction> {
		val getIns = ins.usedRegisters().intersect(spills).flatMap { accMap[it]!!.get }
		val setIns = ins.definedRegisters().intersect(spills).flatMap { accMap[it]!!.set }
		val final = getIns + ins + setIns
		if (final.size == 1) {
			// this means no spilled registers in instruction
			return final
		}
		val spillRegisters = (ins.usedRegisters() + ins.definedRegisters()).intersect(spills)

		if (spillRegisters.size > swapRegisters.size) {
			throw IllegalArgumentException(
				"amount of spill registers in single function (${spillRegisters.size}) greater than amount of swap registers (${swapRegisters.size})",
			)
		}

		val mapping = spillRegisters.zip(swapRegisters).toMap()
		return final.map { InstructionWithRemappedRegisters(it, mapping) }
	}

	/**class holding [get] and [set] instructions for Virtual Register [vr]*/
	private data class vrAccess(
		val get: List<Instruction>,
		val set: List<Instruction>,
		val vr: VirtualRegister,
	)

	/**generate [vrAccess] for [vr]*/
	private fun getAccess(
		vr: VirtualRegister,
		ffm: FunctionFrameManager,
	): vrAccess {
		val mem = ffm.alloc_frame_memory()
		val getIns = ic.cover(AssignmentTree(RegisterTree(vr), mem), null)
		val setIns = ic.cover(AssignmentTree(mem, RegisterTree(vr)), null)

		return vrAccess(getIns, setIns, vr)
	}

	/**[original] instruction with Registers remapped according to [mapping]*/
	private class InstructionWithRemappedRegisters(
		val original: Instruction,
		val mapping: Map<Register, PhysicalRegister>,
	) : Instruction {
		override fun toAsm(newMapping: Map<Register, PhysicalRegister>): String = original.toAsm(newMapping + mapping)

		override fun usedRegisters(): List<Register> = original.usedRegisters().map { mapping.getOrDefault(it, it) }

		override fun definedRegisters(): List<Register> = original.definedRegisters().map { mapping.getOrDefault(it, it) }

		override fun isCopy(): Boolean = original.isCopy()
	}
}
