package org.exeval.instructions

import org.exeval.cfg.Register
import org.exeval.instructions.interfaces.RegisterGraph
import org.exeval.utilities.FindUnion

class CoalescenceGraphCreator {
	public fun createCoalescenceGraph(
		graph: RegisterGraph,
		copyGraph: RegisterGraph,
		domain: Set<Register>,
		numOfColors: Int,
	): RegisterGraph {
		val mapDomainToInt: MutableMap<Register, Int> = mutableMapOf()
		domain.forEachIndexed { index, register ->
			mapDomainToInt[register] = index
		}

		val mapIntToDomain: MutableMap<Int, Register> = mutableMapOf()
		mapDomainToInt.entries.forEach { (key, value) ->
			mapIntToDomain[value] = key
		}

		val findUnion = FindUnion(mapDomainToInt.size)
		val connectionIntGraph = createGraphFromRegisterGraph(graph, mapDomainToInt)
		val copyIntGraph = createGraphFromRegisterGraph(copyGraph, mapDomainToInt)
		var leftoverVertices: MutableSet<Int> = mutableSetOf()
		var verticesToConsider: ArrayDeque<Int> = ArrayDeque()
		(0..mapDomainToInt.size - 1).forEach { x ->
			verticesToConsider.add(x)
		}

		var hasMerged = false
		while (!verticesToConsider.isEmpty() || !leftoverVertices.isEmpty()) {
			if (verticesToConsider.isEmpty()) {
				if (!hasMerged) {
					break
				}

				leftoverVertices.forEach { x ->
					verticesToConsider.add(x)
				}
				hasMerged = false
				leftoverVertices = mutableSetOf()
			}
			val vertex = verticesToConsider.removeFirst()
			if (vertex != findUnion.find(vertex)) {
				continue
			}
			if (!copyIntGraph.contains(vertex)) {
				continue
			}
			for (copyFriend in copyIntGraph[vertex]!!) {
				if (checkIfMergeIsSafe(connectionIntGraph, vertex, copyFriend, numOfColors)) {
					findUnion.union(vertex, copyFriend)
					val vertexLeader = findUnion.find(vertex)
					if (vertexLeader == vertex) {
						mergeToVertex(connectionIntGraph, copyIntGraph, vertex, copyFriend)
						leftoverVertices.add(vertex)
					} else {
						mergeToVertex(connectionIntGraph, copyIntGraph, copyFriend, vertex)
						leftoverVertices.add(copyFriend)
					}
					break
				}
			}
		}

		return createGraphFromFindUnion(findUnion, mapIntToDomain)
	}

	private fun createGraphFromRegisterGraph(
		graph: RegisterGraph,
		mapDomainToInt: MutableMap<Register, Int>,
	): MutableMap<Int, MutableSet<Int>> {
		var result: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
		for (key in graph.keys) {
			val neighbourhood: MutableSet<Int> = mutableSetOf()
			for (neighbour in graph[key]!!) {
				neighbourhood.add(mapDomainToInt[neighbour]!!)
			}
			result[mapDomainToInt[key]!!] = neighbourhood
		}
		return result
	}

	private fun createGraphFromFindUnion(
		findUnion: FindUnion,
		mapIntToDomain: MutableMap<Int, Register>,
	): RegisterGraph {
		val mapLeaderToGroup: MutableMap<Int, MutableSet<Register>> = mutableMapOf()
		for (key in mapIntToDomain.keys) {
			val leader = findUnion.find(key)
			if (!mapLeaderToGroup.containsKey(leader)) {
				mapLeaderToGroup[leader] = mutableSetOf()
			}
			mapLeaderToGroup[leader]!!.add(mapIntToDomain[key]!!)
		}

		val result: MutableMap<Register, Set<Register>> = mutableMapOf()
		for (key in mapIntToDomain.keys) {
			val leader = findUnion.find(key)
			result[mapIntToDomain[key]!!] = mapLeaderToGroup[leader]!!
		}
		return result
	}

	private fun checkIfMergeIsSafe(
		graph: MutableMap<Int, MutableSet<Int>>,
		u: Int,
		v: Int,
		numOfColors: Int,
	): Boolean {
		var uNeighbourhood: Set<Int> = setOf()
		var vNeighbourhood: Set<Int> = setOf()

		if (graph.containsKey(u)) {
			uNeighbourhood = graph[u]!!
		}

		if (graph.containsKey(v)) {
			vNeighbourhood = graph[v]!!
		}

		if (uNeighbourhood.contains(v) || vNeighbourhood.contains(u)) {
			return false
		}

		return checksIfBriggsConditionIsMet(graph, uNeighbourhood, vNeighbourhood, numOfColors) ||
			checksIfGeorgeConditionIsMet(graph, uNeighbourhood, vNeighbourhood, numOfColors)
	}

	private fun checksIfBriggsConditionIsMet(
		graph: MutableMap<Int, MutableSet<Int>>,
		uNeighbourhood: Set<Int>,
		vNeighbourhood: Set<Int>,
		numOfColors: Int,
	): Boolean {
		var newNeighbourhood = uNeighbourhood + vNeighbourhood
		var numOfProblematicVertexes = 0
		for (vertex in newNeighbourhood) {
			if (!graph.containsKey(vertex)) {
				continue
			}
			if (graph[vertex]!!.size >= numOfColors) {
				numOfProblematicVertexes++
			}
			if (numOfProblematicVertexes >= numOfColors) {
				return false
			}
		}
		return true
	}

	private fun checksIfGeorgeConditionIsMet(
		graph: MutableMap<Int, MutableSet<Int>>,
		uNeighbourhood: Set<Int>,
		vNeighbourhood: Set<Int>,
		numOfColors: Int,
	): Boolean {
		for (uFriend in uNeighbourhood) {
			if (!vNeighbourhood.contains(uFriend) && graph.containsKey(uFriend) && graph[uFriend]!!.size >= numOfColors) {
				return false
			}
		}
		return true
	}

	private fun mergeToVertex(
		graph: MutableMap<Int, MutableSet<Int>>,
		copyGraph: MutableMap<Int, MutableSet<Int>>,
		v: Int,
		u: Int,
	) {
		mergeToVertexSingleGraph(graph, v, u)
		mergeToVertexSingleGraph(copyGraph, v, u)
	}

	private fun mergeToVertexSingleGraph(
		graph: MutableMap<Int, MutableSet<Int>>,
		v: Int,
		u: Int,
	) {
		if (!graph.containsKey(u)) {
			return
		}

		if (!graph.containsKey(v)) {
			graph[v] = graph[u]!!
		} else {
			graph[v] = (graph[v]!! + graph[u]!!).toMutableSet()
		}

		for (friend in graph[u]!!) {
			graph[friend]!!.remove(u)
			if (friend != v) {
				graph[friend]!!.add(v)
			}
		}
		graph.remove(u)
	}
}
