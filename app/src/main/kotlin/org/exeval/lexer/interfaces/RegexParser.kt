package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.Regex

interface RegexParser {
    fun parse(pattern: String): Regex
}