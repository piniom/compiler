package org.exeval.automata.tools

import java.util.LinkedList
import java.util.Queue
import org.exeval.automata.interfaces.DFAEquivalenceChecker
import org.exeval.automata.interfaces.DFA
import kotlin.collections.mutableMapOf

class DFAEquivalenceCheckerTool : DFAEquivalenceChecker {

    override fun <S, T>areEquivalent(dfa1: DFA<S>, dfa2: DFA<T>): Boolean {

        // check if both dfa accepts empty string
        if (dfa1.isAccepting(dfa1.startState).xor(dfa2.isAccepting(dfa2.startState)))
            return false

        // prefere to bfs over the graph of states from both dfa
        val visitedMap = mutableMapOf<S, MutableMap<T, Boolean>>()
        val queue: Queue<Pair<S,T>> = LinkedList()
        queue.add(Pair(dfa1.startState, dfa2.startState))
        setState(visitedMap, dfa1.startState, dfa2.startState, true)

        // bfs over the graph of states of dfas to find a state that can differ given dfas
        while(!queue.isEmpty()) {
            val node = queue.poll()
            val firstMap = dfa1.transitions(node.first)
            val secondMap = dfa2.transitions(node.second)

            // check if all possible moves from first dfa can differentiate it from the second dfa
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

            // check if all possible moves from second dfa can differentiate it from the first dfa
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

    ///<summary>
    // returns true if bfs should continue from the new state
    // returns false if bfs should end and returns false as a result
    // returns null if the state should be skipped
    ///</summary>
    private fun <S, T>checkIfShouldCountinue(dfa1: DFA<S>, dfa2: DFA<T>,
     entry: Map.Entry<Char, S>, secondMap: Map<Char, T>): Boolean? {
        if (secondMap[entry.key] == null && !dfa1.isDead(entry.value))
            return false
        if (secondMap[entry.key] == null)
            return null

        val secondNode = secondMap[entry.key]!!

        // checks if only one is accepting or dead
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