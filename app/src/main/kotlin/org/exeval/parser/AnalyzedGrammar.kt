package org.exeval.parser

data class AnalyzedGrammar<S> (
    val nullable: Set<S>,
    val firstProduct: Map<S, List<S>>,
    val grammar: Grammar<S>,
)
