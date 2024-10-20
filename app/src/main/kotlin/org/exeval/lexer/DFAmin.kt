package org.exeval.lexer

import org.exeval.automata.interfaces.DFA
import org.exeval.lexer.interfaces.DFAMinimizer

class DFAmin<S> : DFAMinimizer<S> {
    private val debug = false
    private class minDFA<S> : DFA<S>{
        override val startState: S
        var acceptStates = mutableSetOf<S>()
        public var tmap = mutableMapOf<S,MutableMap<Char,S>>()
        constructor(start:S,acs:MutableSet<S>,t:MutableMap<S,MutableMap<Char,S>>){
            startState = start
            acceptStates = acs
            tmap = t
        }
        override fun isDead(state: S): Boolean{
            //a quick note: dead states will be consolidated during minimization
            return !isAccepting(state) && transitions(state).all{it.value == state}
        }
        override fun isAccepting(state: S): Boolean{
            return acceptStates.contains(state)
        }
        override fun transitions(state: S): Map<Char, S>{
            return tmap[state]!!.toMap()
        }
    }
    fun getStates(dfa:DFA<S>):MutableSet<S>{
        //return a set of reachable states - remove unreachable
        var processing = mutableSetOf(dfa.startState)
        var processed = mutableSetOf<S>()
        while(processing.isNotEmpty()){
            var s:S = processing.elementAt(0)!!
            processing.remove(s)
            processed.add(s)
            dfa.transitions(s).forEach{
                if(!processed.contains(it.value)){
                    processing.add(it.value)
                }
            }
        }
        return processed
    }
    override fun minimize(dfa: DFA<S>):DFA<S>{
        //preprocessing - transition map, list of accept states
        var states = getStates(dfa)
        var tmap = mutableMapOf<S,MutableMap<Char,S>>()
        var asts = mutableSetOf<S>()
        var nsts = mutableSetOf<S>()
        states.forEach{
            tmap[it]=dfa.transitions(it).toMutableMap()
            if(dfa.isAccepting(it)){
                asts.add(it)
            }else{
                nsts.add(it)
            }
        }
        var pastSets = mutableSetOf(asts,nsts)
        while(true){
            fun setOf(s:S):MutableSet<S>{
                return pastSets.find{it.contains(s)}!!
            }
            //build new partition
            //1 - create signature for each state - map: Character->Partition transitions
            if(debug){println(pastSets)}
            var signatures = mutableMapOf<S,MutableMap<Char,MutableSet<S>>>()
            tmap.forEach{
                var s = it.key
                it.value.forEach{
                    var v = it.value
                    signatures.getOrPut(s){mutableMapOf<Char,MutableSet<S>>()}[it.key] = setOf(v)
                }
            }
            //2 - group states by signature
            //looks complicated - states are put into set, A,B are in the same set if they were part of the same partition and have the same signature
            //'belong to the same partition' is required to prevent partitions merging, which can create infinite cycles
            var groups = mutableMapOf<Pair<MutableMap<Char,MutableSet<S>>,MutableSet<S>>,MutableSet<S>>()
            signatures.forEach{
                groups.getOrPut(
                    Pair<MutableMap<Char,MutableSet<S>>,MutableSet<S>>(it.value,setOf(it.key)))
                    {mutableSetOf<S>()}.add(it.key)
            }
            //3 - groups are new partitions
            var newSets = groups.values.toMutableSet()
            //check if partitions are different
            if(newSets == pastSets){
                break
            }else{
                pastSets = newSets
            }
        }
        //create new automata with groups
        var stateMap = mutableMapOf<S,S>() //translation, old to new
        var newStates = mutableSetOf<S>() //set of new states
        pastSets.forEach{
            var s = it.elementAt(0)
            newStates.add(s)
            it.forEach{
                stateMap[it]=s
            }
        }
        //translated transition map
        var newTMap = mutableMapOf<S,MutableMap<Char,S>>()
        newStates.forEach{
            var s = it
            newTMap[s] = mutableMapOf<Char,S>()
            tmap[s]!!.forEach{
                newTMap[s]!![it.key] = stateMap[it.value]!!
            }
        }
        return minDFA(
            stateMap[dfa.startState]!!,
            asts.map{stateMap[it]!!}.toMutableSet(),
            newTMap)
    }
}