package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.automata.interfaces.NFA
import org.exeval.lexer.interfaces.DFAParser

class DFAPars : DFAParser<Int> {
    private var nextConfigNumber: Int = 0
    private var configsNumbers: MutableMap<Set<Int>, Int> = mutableMapOf();

    class ParsedDFA<Int> : DFA<Int> {
        override val startState: Int
        var acceptStates = mutableSetOf<Int>()
        public var tmap = mutableMapOf<Int,MutableMap<Char,Int>>()
        constructor(start:Int,acs:MutableSet<Int>,t:MutableMap<Int,MutableMap<Char,Int>>){
            startState = start
            acceptStates = acs
            tmap = t
        }
        override fun isDead(state: Int): Boolean{
            return !isAccepting(state) && transitions(state).all{it.value == state}
        }
        override fun isAccepting(state: Int): Boolean{
            return acceptStates.contains(state)
        }
        override fun transitions(state: Int): Map<Char, Int>{
            return tmap[state]!!.toMap()
        }
    }

    override fun parse(nfa: NFA<Int>): DFA<Int> {
        var processedConfigs = mutableSetOf<Int>()
        var configsTransitions = mutableMapOf<Int, MutableMap<Char, Int>>()
        var startConfig = nfa.eTransitions(nfa.startState) + nfa.startState
        var workList = ArrayDeque<Pair<Int, Set<Int>>>()
        var startConfigNumber = getConfigNumber(startConfig)
        var acceptingConfigsNumbers = mutableSetOf<Int>()
        if (containsAcceptingState(nfa, startConfig)) {
            acceptingConfigsNumbers.add(startConfigNumber)
        }
        workList.addLast(Pair(startConfigNumber, startConfig))
        processedConfigs.add(startConfigNumber)
        while (workList.isNotEmpty()) {
            var (configNumber, config) = workList.removeFirst()
            for (c in 0..255) {
                var newConfig = delta(nfa, config, c.toChar())
                var newConfigNumber = getConfigNumber(newConfig)
                configsTransitions.getOrPut(configNumber){mutableMapOf()}[c.toChar()] = newConfigNumber
                if (!processedConfigs.contains(newConfigNumber)) {
                    processedConfigs.add(newConfigNumber)
                    if (containsAcceptingState(nfa, newConfig)) {
                        acceptingConfigsNumbers.add(newConfigNumber)
                    }
                    workList.addLast(Pair(newConfigNumber, newConfig))
                }
            }
        }

        return ParsedDFA<Int>(
            startConfigNumber, acceptingConfigsNumbers, configsTransitions
        )
    }

    private fun containsAcceptingState(nfa: NFA<Int>, states: Set<Int>): Boolean {
        return states.any { s ->  nfa.acceptingState == s }
    }

    private fun getConfigNumber(config: Set<Int>): Int {
        return (configsNumbers[config] ?: run {
            nextConfigNumber++
            configsNumbers[config] = nextConfigNumber
            nextConfigNumber
        })
    }

    fun delta(nfa: NFA<Int>, states: Set<Int>, c: Char): Set<Int> {
        return states.fold(mutableSetOf<Int>()) {
            acc, state ->
                var nextState = nfa.transitions(state)[c];
                nextState?.let {
                    acc.add(nextState);
                    getAllETransitions(nfa, nextState).forEach { s -> acc.add(s) };
                }
                acc
        }.toSet()
    }

    private fun getAllETransitions(nfa: NFA<Int>, state: Int): Set<Int> {
        var resultSet = mutableSetOf<Int>()
        var workList = ArrayDeque<Int>()
        workList.add(state)
        var checkedStates = mutableSetOf<Int>()
        while (workList.isNotEmpty()) {
            var s = workList.removeFirst()
            var eTStates = nfa.eTransitions(s)
            resultSet.addAll(eTStates)
            workList.addAll(eTStates.filter { el -> !checkedStates.contains(el) })
            checkedStates.add(s)
        }

        return resultSet
    }
} 