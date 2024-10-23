package org.exeval.parser

import org.exeval.parser.Grammar

data class AnalyzedGrammar<S> (
    val nullable: Set<S>,
    val firstProduct: Map<S, List<S>>,
    val grammar: Grammar<S>,
)
