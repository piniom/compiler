package org.example.automata;

import kotlin.collections.mutableMapOf
import java.util.LinkedList
import java.util.Queue

class DFAEquivalentChecker {

    fun <S, T>areEquivalent(dfa1: IDFA<S>, dfa2: IDFA<T>): Boolean {
        if (dfa1.isAccepting(dfa1.startState).xor(dfa2.isAccepting(dfa2.startState)))
            return false
        val visitedMap = mutableMapOf<S, MutableMap<T, Boolean>>()
        val queue: Queue<Pair<S,T>> = LinkedList()
        queue.add(Pair(dfa1.startState, dfa2.startState))
        setState(visitedMap, dfa1.startState, dfa2.startState, true)
        while(!queue.isEmpty()) {
            val node = queue.poll()
            val firstMap = dfa1.transitions(node.first)
            val secondMap = dfa2.transitions(node.second)
            for (entry in firstMap.entries.iterator()) {
                val checkState = checkIfShouldCountinue(dfa1, dfa2, entry, secondMap)
                if (checkState == null)
                    continue
                if (!checkState)
                    return false
                val secondNode = secondMap[entry.key]!!
                if (getState(visitedMap, entry.value, secondNode))
                    continue
                setState(visitedMap, entry.value, secondNode, true)
                queue.add(Pair(entry.value, secondNode))
            }
            
            for (entry in secondMap.entries.iterator()) {
                val checkState = checkIfShouldCountinue(dfa2, dfa1, entry, firstMap)
                if (checkState == null)
                    continue
                if (!checkState)
                    return false
                val secondNode = firstMap[entry.key]!!
                if (getState(visitedMap, secondNode, entry.value))
                    continue
                setState(visitedMap, secondNode, entry.value, true)
                queue.add(Pair(secondNode, entry.value))
            }
        }
        return true
    }

    private fun <S, T>checkIfShouldCountinue(dfa1: IDFA<S>, dfa2: IDFA<T>,
     entry: Map.Entry<Char, S>, secondMap: Map<Char, T>): Boolean? {
        if (secondMap[entry.key] == null && !dfa1.isDead(entry.value))
            return false
        if (secondMap[entry.key] == null)
            return null
        val secondNode = secondMap[entry.key]!!
        if (dfa2.isDead(secondNode).xor(dfa1.isDead(entry.value)) || 
         dfa2.isAccepting(secondNode).xor(dfa1.isAccepting(entry.value)))
            return false
        return true
    }

    private fun <S, T>setState(map: MutableMap<S, MutableMap<T, Boolean>>, state1: S,
     state2: T, value: Boolean) {
        if (map[state1] == null) {
            map[state1] = mutableMapOf<T, Boolean>()
        }
        map[state1]!![state2] = value
    }

    private fun <S, T>getState(map: MutableMap<S, MutableMap<T, Boolean>>,
     state1: S, state2: T): Boolean {
        return if (map[state1] == null || map[state1]!![state2] == null) false else map[state1]!![state2]!! 
    }
}