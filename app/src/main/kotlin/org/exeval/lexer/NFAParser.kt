package org.exeval.lexer

import org.exeval.lexer.interfaces.NFAParser
import org.exeval.automata.interfaces.NFA
import org.exeval.automata.interfaces.Regex
import kotlin.jvm.internal.iterator
import kotlin.collections.mutableMapOf


public class NFAParserImpl: NFAParser{

    val stateFabric: (Int) -> Any 

    constructor(fabric: (Int) -> Any){
        stateFabric = fabric
    }
    inner class NFAImpl<Any>: NFA<Any>{

        var trans = mutableMapOf<Any, MutableMap<Char, Any>>()
        var eTrans = mutableMapOf<Any, MutableSet<Any>>()
        override val startState: Any 
        override val acceptingState: Any

        constructor(start:Any, acs:Any, c: Char) {
            startState = start
            acceptingState = acs

            trans.computeIfAbsent(startState) { mutableMapOf() }[c] = acceptingState

            collapseEpsilonTransitions()
        }

        constructor(start: Any, acs: Any, nfas: List<NFAImpl<Any>>, regex: Regex){
            startState = start
            acceptingState = acs

            for (nfa in nfas) {
                trans.putAll(nfa.trans)
                eTrans.putAll(nfa.eTrans)
            }

            when(regex){
                is Regex.Union ->{
                    eTrans.computeIfAbsent(startState) { mutableSetOf() }
                    for (nfa in nfas) {
                        eTrans[startState]!!.add(nfa.startState)
                        eTrans.computeIfAbsent(nfa.acceptingState) { mutableSetOf() }.add(acceptingState)
                    }
                }
                is Regex.Concat -> {
                    for (i in 0 until nfas.size - 1) {
                        val currentNFA = nfas[i]
                        val nextNFA = nfas[i + 1]
                        eTrans.computeIfAbsent(currentNFA.acceptingState) { mutableSetOf() }.add(nextNFA.startState)
                    }

                    eTrans.computeIfAbsent(startState) { mutableSetOf() }.add(nfas.first().startState)
                    eTrans.computeIfAbsent(nfas.last().acceptingState) { mutableSetOf() }.add(acceptingState)
                }
                else -> {

                }
            }

            collapseEpsilonTransitions()
        }

        constructor(start: Any, acs: Any, nfa: NFAImpl<Any>, regex: Regex) {
            startState = start
            acceptingState = acs

            trans.putAll(nfa.trans)
            eTrans.putAll(nfa.eTrans)

            eTrans.computeIfAbsent(startState) { mutableSetOf() }
            eTrans.computeIfAbsent(nfa.acceptingState) { mutableSetOf() }

            when(regex){
                is Regex.Star ->{
                    eTrans[startState]!!.add(nfa.startState)
                    eTrans[startState]!!.add(acceptingState)
                    eTrans[nfa.acceptingState]!!.add(nfa.startState)
                    eTrans[nfa.acceptingState]!!.add(acceptingState)
                }
                else -> {

                }
            }

            collapseEpsilonTransitions()
        }


        override fun transitions(state: Any): Map<Char, Any>{
            return trans[state]!!.toMap()
        }
        
        override fun eTransitions(state: Any): Set<Any>{
            return eTrans[state]!!.toSet()
        }

        private fun collapseEpsilonTransitions() {
            val newETrans = mutableMapOf<Any, MutableSet<Any>>()
            val states = mutableSetOf<Any>()

            states.addAll(trans.keys)

            for (valueMap in trans.values) {
                states.addAll(valueMap.values)
            }

            states.add(startState)
            states.add(acceptingState)

            for (state in states) {
                newETrans[state] = computeDirectEpsilonClosure(state, states)
            }

            eTrans = newETrans
        }

        private fun computeDirectEpsilonClosure(state: Any, states: Set<Any>): MutableSet<Any> {
            val visited = mutableSetOf<Any>()
            val stack = ArrayDeque<Any>()
            stack.add(state)

            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
                if (visited.add(current)) {
                    val neighbors = eTrans[current] ?: emptySet()
                    stack.addAll(neighbors)
                }
            }

            visited.remove(state)

            visited.retainAll(states)

            return visited
        }
    }

    fun parseInner(regex: Regex, cnt: Int): NFAImpl<Any> {
        var mutCnt: Int = cnt
        val startState = stateFabric(mutCnt++)
        val acceptingState = stateFabric(mutCnt++)
        return when(regex){
            is Regex.Atom -> NFAImpl(startState, acceptingState, regex.char)
            is Regex.Star-> {
                val innerNFA = parseInner(regex.expression, mutCnt)
                mutCnt += 2
                NFAImpl(startState, acceptingState, innerNFA, regex)
            }
            is Regex.Union -> {
                val nfas = regex.expressions.map { expression ->
                    val nfa = parseInner(expression, mutCnt)
                    mutCnt += nfa.trans.count { it.value.isNotEmpty() } + nfa.eTrans.count { it.value.isNotEmpty() }
                    if (nfa.trans.isNotEmpty()) mutCnt += 1
                    if (nfa.eTrans.isNotEmpty() && nfa.eTrans.values.any { it.isNotEmpty() }) mutCnt += 1
                    nfa
                }

                NFAImpl(startState, acceptingState, nfas, regex)
            } 
            is Regex.Concat->{
                val nfas = regex.expressions.map { expression ->
                    val nfa = parseInner(expression, mutCnt)
                    mutCnt += nfa.trans.count { it.value.isNotEmpty() } + nfa.eTrans.count { it.value.isNotEmpty() }
                    if (nfa.trans.isNotEmpty()) mutCnt += 1
                    if (nfa.eTrans.isNotEmpty() && nfa.eTrans.values.any { it.isNotEmpty() }) mutCnt += 1
                    nfa
                }

                NFAImpl(startState, acceptingState, nfas, regex)
            }
        }
    }

    override fun parse(regex: Regex): NFA<Any>{
        return parseInner(regex, 0)
    }

}
