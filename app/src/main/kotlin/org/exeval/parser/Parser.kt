package org.exeval.parser

import org.exeval.parser.interfaces.ParseTree

class Parser<S> {
    constructor(grammar: AnalyzedGrammar<S>) {
        //...
    }

    fun run(leaves: List<ParseTree.Leaf<S>>) {
        //...
    }
}
