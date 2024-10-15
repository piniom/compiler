package org.example.automata.interfaces

import org.example.automata.IDFA

interface IDFAEquivaleceChecker<S> {
    fun areEquivalent(dfa1: IDFA<S>, dfa2: IDFA<S>): Boolean
}