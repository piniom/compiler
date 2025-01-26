package org.exeval.parser.parser

import org.exeval.parser.interfaces.ParseTree

interface OneTimeParser<S> {
    fun run(): ParseTree.Branch<S>

    interface Factory<S> {
        fun create(leaves: List<ParseTree.Leaf<S>>): OneTimeParser<S>
    }
}