import org.exeval.lexer.interfaces.NFAParser
import org.exeval.automata.interfaces.NFA


class NFAParserImpl<S>: NFAParser<S>{

    val stateFabric: (Int) -> S

    constructor(fabric: (Int) -> S){
        stateFabric = fabric
    }
    inner class NFAImpl: NFA<S>{

        var stateCount: Int
        override val startState: S 
        override val acceptingState: S 

        public constructor(){
            startState = stateFabric(0) 
            acceptingState = stateFabric(1) 
            stateCount = 2 
        }
         //constructor(nfa1: NFAImpl<S>, nfa2: NFAImpl<S>){
         //    stateCount = nfa1.stateCount + nfa2.stateCount + 2
         //    startState = 
         //}

        override fun transitions(state: S): Map<Char, S>{
            return emptyMap<Char, S>()
        }
        
        override fun eTransitions(state: S): Set<S>{
            return emptySet<S>()
        }
    }

    override fun parse(regex: Regex): NFA<S> {
        NFAImpl() 
    }

}
