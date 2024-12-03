package org.exeval.instructions.interfaces

import org.exeval.cfg.Register
import org.exeval.cfg.PhysicalRegister

interface RegisterAllocator {
	fun allocate(livenessResult: LivenessResult, domain: List<Register>, range: List<PhysicalRegister>): AllocationResult
}

data class AllocationResult(
	val mapping: Map<Register, PhysicalRegister>,
	val spills: List<Register>
)
