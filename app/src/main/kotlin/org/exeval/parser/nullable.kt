package org.exeval.grammar

import org.exeval.parser.Grammar
import org.exeval.parser.AnalyzedGrammar

class nullable{
    /*
    note to testers:
    empty sybol can be set, or can be denoted as empty transition ('A'->list())

    */
    companion object{
        fun <C> getNullable(grammar: Map<C,Set<List<C>>>, empty: C?):Set<C>{
            var nullable : MutableSet<C> = if(empty != null) mutableSetOf(empty) else mutableSetOf()
            while(true){
                var newNullable = nullable.toMutableSet()
                grammar.forEach{
                    var c = it.key
                    if(it.value.any{
                        it.all{
                            nullable.contains(it)
                        }
                    }){
                        newNullable.add(c)
                    }
                }
                if(newNullable == nullable){
                    break;
                }else{
                    nullable = newNullable
                }
            }
            return nullable
        }
        fun <C>analyse(g: Grammar<C>,empty:C?=null):AnalyzedGrammar<C>{
            var transitions: Map<C,MutableSet<List<C>>> = mapOf()
            g.productions.forEach{
                transitions.getOrDefault(it.left, mutableSetOf()).add(it.right)
            }
            return AnalyzedGrammar(
                getNullable(transitions,empty),
                mapOf(),
                g)
        }
    }
}