package org.exeval.automata

import org.exeval.automata.interfaces.DFA
import org.exeval.input.interfaces.Location
class DFAWalker<S>(start: Location, private val dfa: DFA<S>) : Comparable<DFAWalker<*>> {
    private var currentState: S
    private var _maxAccepting: Location?

    val maxAccepting: Location?
        get() = _maxAccepting
    private var _maxAcceptingCount: Int = 0

    val maxAcceptingCount: Int
        get() = _maxAcceptingCount
    private var count: Int = 0
    init {
        this.currentState = dfa.startState
        this._maxAccepting = if (dfa.isAccepting(currentState)) start else null;
        this._maxAcceptingCount = if (dfa.isAccepting(currentState)) 0 else -1;
    }

    public fun isDead(): Boolean {
        return this.dfa.isDead(this.currentState)
    }

    private fun isAccepting(): Boolean {
        return this.dfa.isAccepting(this.currentState)
    }

    public fun transition(c: Char, loc: Location): Boolean {
        this.currentState = dfa.transitions(this.currentState)[c] ?: return false;
        this.count++
        if (this.isAccepting()) {
            this._maxAccepting = loc
            this._maxAcceptingCount = this.count
        }

        return true
    }

    override fun compareTo(other: DFAWalker<*>): Int {
        return this._maxAcceptingCount - other._maxAcceptingCount
    }
}