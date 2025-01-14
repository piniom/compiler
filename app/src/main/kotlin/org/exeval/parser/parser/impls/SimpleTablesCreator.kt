package org.exeval.parser.parser.impls

import org.exeval.parser.parser.Actions
import org.exeval.parser.parser.Goto
import org.exeval.parser.parser.TablesCreator
import org.exeval.parser.parser.TablesCreator.Tables


class SimpleTablesCreator<Symbol, OldState>(tablesCreator: TablesCreator<Symbol, OldState>) :
    TablesCreator<Symbol, Int> {
    override val tables: Tables<Symbol, Int>

    private val stateMap: MutableMap<OldState, Int> = mutableMapOf()

    init {
        val oldTables = tablesCreator.tables
        iterateOverActions(oldTables.actions)
        iterateOverGoto(oldTables.goto)

        val startState = stateMap[oldTables.startState]!!
        val newActions = createNewActions(oldTables.actions)
        val newGoto = createNewGoto(oldTables.goto)

        tables = Tables(startState, newActions, newGoto)
    }


    private fun createNewActions(actions: Actions<Symbol, OldState>): Actions<Symbol, Int> {
        return actions.asSequence().map { (key, value) ->
            val newKey = key.first to stateMap[key.second]!!
            val newValue: TablesCreator.Action<Symbol, Int> = when (value) {
                is TablesCreator.Action.Accept -> TablesCreator.Action.Accept()
                is TablesCreator.Action.Reduce -> TablesCreator.Action.Reduce(value.production)
                is TablesCreator.Action.Shift -> TablesCreator.Action.Shift(stateMap[value.state]!!)
            }
            newKey to newValue
        }.toMap()
    }

    private fun createNewGoto(goto: Goto<Symbol, OldState>): Goto<Symbol, Int> {
        return goto.asSequence().map { (key, value) ->
            val newKey = key.first to stateMap[key.second]!!
            val newValue = stateMap[value]!!
            newKey to newValue
        }.toMap()
    }

    private fun iterateOverActions(actions: Actions<Symbol, OldState>) {
        actions.forEach { (key, action) ->
            encounterState(key.second)
            if (action is TablesCreator.Action.Shift) {
                encounterState(action.state)
            }
        }
    }

    private fun iterateOverGoto(goto: Goto<Symbol, OldState>) {
        goto.forEach { (key, valState) ->
            encounterState(key.second)
            encounterState(valState)
        }
    }

    private var unusedStateId = 0
    private fun encounterState(state: OldState) {
        if (state in stateMap) return

        stateMap[state] = unusedStateId
        unusedStateId += 1
    }
}