package org.exeval.instructions.interfaces

import org.exeval.cfg.Register
import org.exeval.instructions.linearizer.BasicBlock

interface LivenessChecker {
	fun check(basicBlocks: List<BasicBlock>): LivenessResult
}

data class LivenessResult(
	val interference: RegisterGraph,
	val copy: RegisterGraph,
)

typealias RegisterGraph = Map<Register, Set<Register>>
typealias MutableRegisterGraph = MutableMap<Register, MutableSet<Register>>
