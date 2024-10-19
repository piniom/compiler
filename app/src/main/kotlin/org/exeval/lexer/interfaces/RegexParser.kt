package org.exeval.lexer.interfaces

import org.exeval.automata.interfaces.Regex

interface RegexParser {
    // Should throw BadRegexFormatException if pattern is invalid
    fun parse(pattern: String): Regex
}

class BadRegexFormatException(message: String) : Exception(message)