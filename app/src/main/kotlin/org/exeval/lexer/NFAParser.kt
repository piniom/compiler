import org.exeval.lexer.interfaces.NFAParser
import org.exeval.automata.interfaces.NFA
import org.exeval.automata.interfaces.Regex
import kotlin.jvm.internal.iterator
import kotlin.collections.mutableMapOf


class NFAParserImpl<Any>: NFAParser{

    val stateFabric: (Int) -> Any 

    constructor(fabric: (Int) -> Any){
        stateFabric = fabric
    }
    inner class NFAImpl<S>: NFA<S>{

        var trans = mutableMapOf<S, MutableMap<Char, S>>()
        var eTrans = mutableMapOf<S, MutableSet<S>>()
        override val startState: S 
        override val acceptingState: S 

        constructor(start:S, acs:S, c: Char) {
            startState = start
            acceptingState = acs
            eTrans.put(startState, mutableSetOf<S>())
            eTrans.put(acceptingState, mutableSetOf<S>())
            trans.put(startState, mutableMapOf<Char, S>())
            trans.put(acceptingState, mutableMapOf<Char, S>())
            trans[start]!!.put(c, acceptingState)
        }
        constructor(start: S, acs: S, nfa1: NFAImpl<S>, nfa2: NFAImpl<S>, regex: Regex){
            startState = start
            acceptingState = acs
            eTrans.put(startState, mutableSetOf<S>())
            eTrans.put(acceptingState, mutableSetOf<S>())
            trans.put(startState, mutableMapOf<Char, S>())
            trans.put(acceptingState, mutableMapOf<Char, S>())
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
                else
            }
        }
        constructor(start: S, acs: S, nfa: NFAImpl<S>){
            startState = start
            acceptingState = acs 
            eTrans.put(startState, mutableSetOf<S>())
            eTrans.put(acceptingState, mutableSetOf<S>())
            trans.put(startState, mutableMapOf<Char, S>())
            trans.put(acceptingState, mutableMapOf<Char, S>())
            eTrans[startState]!!.add(nfa.startState)
            eTrans[nfa.acceptingState]!!.add(startState)
        }

        override fun transitions(state: S): Map<Char, S>{
            return trans[state]!!.toMap()
        }
        
        override fun eTransitions(state: S): Set<S>{
            return eTrans[state]!!.toSet()
        }
    }

    fun parseInner(regex: Regex, cnt: Int): NFAImpl<Any> {
        var mutCnt: Int = cnt
        val startState = stateFabric(mutCnt++)
        val acceptingState = stateFabric(mutCnt++)
        return when(regex){
            is Regex.Atom -> NFAImpl(startState, acceptingState, regex.char)
            is Regex.Star-> NFAImpl(startState, acceptingState, parseInner(regex.expression, mutCnt))
            is Regex.Union -> {
                val iter = regex.expressions.iterator()
                var prev = parseInner(iter.next(), mutCnt)
                while(iter.hasNext()){
                    val curr = parseInner(iter.next(), mutCnt)
                    prev = NFAImpl(startState, acceptingState, prev, curr, regex) 
                }
                prev 
            } 
            is Regex.Concat->{
                val iter = regex.expressions.iterator()
                var prev = parseInner(iter.next(), mutCnt)
                while(iter.hasNext()){
                    val curr = parseInner(iter.next(), mutCnt)
                    prev = NFAImpl(startState, acceptingState, prev, curr, regex) 
                }
                prev 
            }
        }
    }

    override fun parse(regex: Regex): NFA<Any>{
        return parseInner(regex, 0)
    }

}
