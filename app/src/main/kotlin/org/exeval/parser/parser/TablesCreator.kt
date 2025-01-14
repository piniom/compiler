package org.exeval.parser.parser

import org.exeval.parser.Production

interface TablesCreator<Symbol, State> {
    val tables: Tables<Symbol, State>

    data class Tables<Symbol, State>(
        val startState: State,
        val actions: Actions<Symbol, State>,
        val goto: Goto<Symbol, State>,
    )

    sealed interface Action<Symbol, State> {
        data class Reduce<Symbol, State>(val production: Production<Symbol>) : Action<Symbol, State>
        data class Shift<Symbol, State>(val state: State) : Action<Symbol, State>
        class Accept<Symbol, State> : Action<Symbol, State>
    }
}

typealias Actions<Symbol, State> = Map<Pair<Symbol, State>, TablesCreator.Action<Symbol, State>>
typealias Goto<Symbol, State> = Map<Pair<Symbol, State>, State>
