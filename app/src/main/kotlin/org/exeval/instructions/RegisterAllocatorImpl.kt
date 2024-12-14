package org.exeval.instructions

import org.exeval.cfg.PhysicalRegister
import org.exeval.cfg.Register
import org.exeval.cfg.VirtualRegister
import org.exeval.instructions.interfaces.AllocationResult
import org.exeval.instructions.interfaces.LivenessResult
import org.exeval.instructions.interfaces.MutableRegisterGraph
import org.exeval.instructions.interfaces.RegisterGraph
import org.exeval.instructions.interfaces.RegisterAllocator

class RegisterAllocatorImpl : RegisterAllocator {

    override fun allocate(
        livenessResult: LivenessResult,
        domain: Set<Register>,
        range: Set<PhysicalRegister>
    ): AllocationResult {

        val mapping: MutableMap<Register, PhysicalRegister> = mutableMapOf()
        val spills: MutableSet<VirtualRegister> = mutableSetOf()
        val graph = livenessResult.interference
        val copyGraph = livenessResult.copy
        /*val possibleRegisters = domain.toList()
        val graphsDomain = domain.toMutableSet()
        val numOfAvailableColors = range.size

        for (register in possibleRegisters) {
            if (!graphsDomain.contains(register))
                continue

            if (!copyGraph.containsKey(register))
                continue
            
            for (copyReg in copyGraph[register]!!) {
                if (!checkIfMergeIsSafe(graph, register, copyReg, numOfAvailableColors))
                    continue
                mergeToVertex(graph, copyGraph, register, copyReg)
                graphsDomain.remove(copyReg)
            }
        }*/

        //...

        

        val coalescence : Map<Register, Set<Register>> = CoalescenceGraphCreator().createCoalescenceGraph(graph, copyGraph, domain, range.size)
        val visited: MutableSet<Register> = domain.filter { it is PhysicalRegister }.toMutableSet()
        val available: MutableSet<Register> = domain.subtract(visited).toMutableSet()
        val order: MutableList<Register> = visited.toMutableList()

        while(visited.size != domain.size) {
            val el = available.minBy {
                graph[it]!!.count { visited.contains(it) }
            }

            available.remove(el)
            visited.add(el)
            order.add(el)
        }

        val colored: MutableSet<Register> = mutableSetOf()
        for (vertex in order) {

            if (mapping.containsKey(vertex))
                continue

            val physicalRegister = coalescence[vertex]!!.firstOrNull { it is PhysicalRegister } as PhysicalRegister?
            if (physicalRegister !== null ) {
                colored.add(vertex)
                coalescence[vertex]!!.forEach {
                    mapping.put(it, physicalRegister)
                }
            } else {
                val availableRegisters = range subtract graph[vertex]!!.filter { colored.contains(it) }.map { mapping[it]!! }
                if (availableRegisters.isEmpty() ) {
                    coalescence[vertex]!!.forEach { spills.add(it as VirtualRegister) }
                } else {
                    colored.add(vertex)
                    coalescence[vertex]!!.forEach { mapping.put(it, availableRegisters.first()) }
                }
            }

        }

        return AllocationResult(mapping, spills)
    }
}
