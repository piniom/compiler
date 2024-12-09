package org.exeval.instructions

import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.instructions.interfaces.AllocationResult
import org.exeval.instructions.interfaces.LivenessResult
import org.exeval.instructions.interfaces.MutableRegisterGraph
import org.exeval.instructions.interfaces.RegisterAllocator

class RegisterAllocatorImpl : RegisterAllocator {
    override fun allocate(
        livenessResult: LivenessResult,
        domain: Set<Register>,
        range: Set<PhysicalRegister>
    ): AllocationResult {
        val mapping: MutableMap<Register, PhysicalRegister> = mutableMapOf()
        val spills: MutableSet<Register> = mutableSetOf()


        val visited: MutableSet<Register> = domain.filter { it is PhysicalRegister }.toMutableSet()
        val available: MutableSet<Register> = domain.subtract(visited).toMutableSet()
        val order: MutableList<Register> = visited.toMutableList()

        // todo: merge regexes based on copy

        while(visited.size != domain.size) {
            val el = available.minBy {
                livenessResult.interference[it]!!.count { visited.contains(it) }
            }

            available.remove(el)
            visited.add(el)
            order.add(el)
        }

        val colored: MutableSet<Register> = mutableSetOf()
        for (register in order) {
            when(register) {
                is PhysicalRegister -> {
                    colored.add(register)
                    mapping.put(register, register)
                }
                else -> {
                    val availableRegisters = range subtract livenessResult.interference[register]!!.filter { colored.contains(it) }.map { mapping[it]!! }
                    if (availableRegisters.isEmpty() ) {
                        spills.add(register)
                    } else {
                        colored.add(register)
                        mapping.put(register, availableRegisters.first())
                    }
                }
            }
        }

        return AllocationResult(mapping, spills)
    }
}