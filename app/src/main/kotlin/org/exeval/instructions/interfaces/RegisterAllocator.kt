package org.exeval.instructions.interfaces

import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.cfg.VirtualRegister

interface RegisterAllocator {
	fun allocate(
		livenessResult: LivenessResult,
		domain: Set<Register>,
		range: Set<PhysicalRegister>,
	): AllocationResult
}

data class AllocationResult(
	val mapping: Map<Register, PhysicalRegister>,
	val spills: Set<VirtualRegister>,
)
