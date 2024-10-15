package org.exeval.automata.interfaces

sealed interface Regex {
    data class Atom(val char: Char): Regex
    data class Union(val expressions: List<Regex>): Regex
    data class Concat(val expressions: List<Regex>): Regex
    data class Star(val expression: Regex): Regex
}