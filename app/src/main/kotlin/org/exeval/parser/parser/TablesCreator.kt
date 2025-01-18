package org.exeval.parser.parser

import kotlinx.serialization.Serializable
import org.exeval.parser.Production

interface TablesCreator<Symbol, State> {
    val tables: Tables<Symbol, State>

    @Serializable
    data class Tables<Symbol, State>(
        val startState: State,
        val actions: Actions<Symbol, State>,
        val goto: Goto<Symbol, State>,
    )

    @Serializable
    sealed interface Action<Symbol, State> {
        @Serializable
        data class Reduce<Symbol, State>(val production: Production<Symbol>) : Action<Symbol, State>

        @Serializable
        data class Shift<Symbol, State>(val state: State) : Action<Symbol, State>

        @Serializable
        class Accept<Symbol, State> : Action<Symbol, State>
    }
}

typealias Actions<Symbol, State> = Map<Pair<Symbol, State>, TablesCreator.Action<Symbol, State>>
typealias Goto<Symbol, State> = Map<Pair<Symbol, State>, State>
