package org.exeval.lexer.regexparser

sealed interface RegexToken {
    data object Union : RegexToken
    data object Star : RegexToken
    data object OpeningBracket : RegexToken
    data object ClosingBracket : RegexToken
    data class Group(val group: Set<Char>) : RegexToken
    data class Atom(val char: Char) : RegexToken
}