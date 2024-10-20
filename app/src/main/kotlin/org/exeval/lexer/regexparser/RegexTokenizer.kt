package org.exeval.lexer.regexparser

import org.exeval.lexer.interfaces.BadRegexFormatException

class RegexTokenizer {
    fun tokenize(pattern: String): List<RegexToken> {
        val res = mutableListOf<RegexToken>()
        var i = 0
        while (i < pattern.length) {
            val cur = pattern[i]
            res.add(when (cur) {
                '*' -> RegexToken.Star
                '|' -> RegexToken.Union
                '(' -> RegexToken.OpeningBracket
                ')' -> RegexToken.ClosingBracket
                '.' -> RegexToken.Group(ASCII)
                '\\' -> run {
                    if (i == pattern.length - 1) throw BadRegexFormatException("You cannot escape the end of the regex")

                    val next = pattern[++i]
                    val groupOfNext = groupMap[next]

                    if (groupOfNext != null) return@run groupOfNext
                    if (next in escapableCharacters) return@run RegexToken.RegexChar(next)
                    throw BadRegexFormatException("Tried to escape character $next at index $i. Cannot do this since this is neither a group nor an escapable character")
                }

                in ASCII -> RegexToken.RegexChar(cur)
                else -> throw BadRegexFormatException("Non-ASCII character $cur at index $i")
            })
            i++
        }
        return res
    }
}

private val ASCII: Set<Char> = (0..127).map { Char(it) }.toSet()
private val LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toSet()
private val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toSet()
private val LETTERS = LOWERCASE union UPPERCASE
private val DIGITS = "0123456789".toSet()
private val IDENTIFIERS = LETTERS union DIGITS union setOf('_')
private val WHITESPACES = listOf(9, 10, 11, 12, 13, 32).map { Char(it) }.toSet()

private val groupMap: Map<Char, RegexToken.Group> = mapOf(
    'a' to RegexToken.Group(LETTERS),
    'A' to RegexToken.Group(ASCII subtract LETTERS),
    'l' to RegexToken.Group(LOWERCASE),
    'L' to RegexToken.Group(ASCII subtract LOWERCASE),
    'u' to RegexToken.Group(UPPERCASE),
    'U' to RegexToken.Group(ASCII subtract UPPERCASE),
    'd' to RegexToken.Group(DIGITS),
    'D' to RegexToken.Group(ASCII subtract DIGITS),
    's' to RegexToken.Group(WHITESPACES),
    'S' to RegexToken.Group(ASCII subtract WHITESPACES),
    'i' to RegexToken.Group(IDENTIFIERS),
    'I' to RegexToken.Group(ASCII subtract IDENTIFIERS),
)

private val escapableCharacters: Set<Char> = setOf('|', '*', '\\', '(', ')', '.')