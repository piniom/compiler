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
            eTrans.put(startState, mutableSetOf<Any>())
            eTrans.put(acceptingState, mutableSetOf<Any>())
            trans.put(startState, mutableMapOf<Char, Any>())
            trans.put(acceptingState, mutableMapOf<Char, Any>())
            trans[start]!!.put(c, acceptingState)
        }

        constructor(start: Any, acs: Any, nfa1: NFAImpl<Any>, nfa2: NFAImpl<Any>, regex: Regex){
            startState = start
            acceptingState = acs

            trans.putAll(nfa1.trans)
            trans.putAll(nfa2.trans)
            eTrans.putAll(nfa1.eTrans)
            eTrans.putAll(nfa2.eTrans)

            eTrans[startState] = mutableSetOf()
            eTrans[acceptingState] = mutableSetOf()
            trans[startState] = mutableMapOf()
            trans[acceptingState] = mutableMapOf()
            when(regex){
                is Regex.Union ->{
                    eTrans[startState]!!.add(nfa1.startState)
                    eTrans[startState]!!.add(nfa2.startState)
                    eTrans[nfa1.acceptingState]!!.add(acceptingState)
                    eTrans[nfa2.acceptingState]!!.add(acceptingState)
                }
                is Regex.Concat -> {
                    eTrans[startState]!!.add(nfa1.startState)
                    eTrans[nfa1.acceptingState]!!.add(nfa2.startState)
                    eTrans[nfa2.acceptingState]!!.add(acceptingState)
                }
                else -> {

                }
            }
        }

        constructor(start: Any, acs: Any, nfa: NFAImpl<Any>, regex: Regex) {
            startState = start
            acceptingState = acs

            trans.putAll(nfa.trans)
            eTrans.putAll(nfa.eTrans)

            eTrans[startState] = mutableSetOf()
            eTrans[acceptingState] = mutableSetOf()
            trans[startState] = mutableMapOf()
            trans[acceptingState] = mutableMapOf()

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
        }


        override fun transitions(state: Any): Map<Char, Any>{
            return trans[state]!!.toMap()
        }
        
        override fun eTransitions(state: Any): Set<Any>{
            return eTrans[state]!!.toSet()
        }
    }

    fun parseInner(regex: Regex, cnt: Int): NFAImpl<Any> {
        var mutCnt: Int = cnt
        val startState = stateFabric(mutCnt++)
        val acceptingState = stateFabric(mutCnt++)
        return when(regex){
            is Regex.Atom -> NFAImpl(startState, acceptingState, regex.char)
            is Regex.Star-> NFAImpl(startState, acceptingState, parseInner(regex.expression, mutCnt), regex)
            is Regex.Union -> {
                val nfas = regex.expressions.map { expression ->
                    val nfa = parseInner(expression, mutCnt)
                    mutCnt += nfa.trans.size + nfa.eTrans.size
                    nfa
                }

                nfas.reduce { combinedNFA, currentNFA ->
                    NFAImpl(startState, acceptingState, combinedNFA, currentNFA, regex)
                }
            } 
            is Regex.Concat->{
                val nfas = regex.expressions.map { expression ->
                    val nfa = parseInner(expression, mutCnt)
                    mutCnt += nfa.trans.size + nfa.eTrans.size
                    nfa
                }

                nfas.reduce { combinedNFA, currentNFA ->
                    NFAImpl(startState, acceptingState, combinedNFA, currentNFA, regex)
                }
            }
        }
    }

    override fun parse(regex: Regex): NFA<Any>{
        return parseInner(regex, 0)
    }

}
