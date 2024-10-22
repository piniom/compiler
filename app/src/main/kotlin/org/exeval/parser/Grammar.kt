package org.exeval.parser

data class Grammar<S> (
  val startSymbol: S,
  val endOfParse: S,
  val productions: List<Production<S>>,
)
