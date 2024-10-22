package org.exeval.parser

class Grammar<S> {
  var start_symbol: S
  var end_of_parse: S

  var productions: List<Prod<S>>
}
